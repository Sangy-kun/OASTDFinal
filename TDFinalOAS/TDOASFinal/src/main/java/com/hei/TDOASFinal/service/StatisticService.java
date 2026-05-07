package com.hei.TDOASFinal.service;

import com.hei.TDOASFinal.config.DatabaseConnection;
import com.hei.TDOASFinal.model.*;
import com.hei.TDOASFinal.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Service
public class StatisticService {

    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;
    private final MembershipFeeRepository feeRepository;
    private final ActivityRepository activityRepository;
    private final AttendanceRepository attendanceRepository;

    public StatisticService(CollectivityRepository collectivityRepository,
                            MemberRepository memberRepository,
                            MembershipFeeRepository feeRepository,
                            ActivityRepository activityRepository,
                            AttendanceRepository attendanceRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
        this.feeRepository = feeRepository;
        this.activityRepository = activityRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public List<CollectivityLocalStatistics> getMemberStatistics(String collectivityId,
                                                                 LocalDate from,
                                                                 LocalDate to) {
        validateCollectivityAndDates(collectivityId, from, to);
        try {
            List<Member> members = memberRepository.findByCollectivityId(collectivityId);
            List<MembershipFee> activeFees = getActiveFees(collectivityId);
            Map<String, Double> collectedPerMember = getCollectedPerMember(collectivityId, from, to);
            Map<String, Map<String, Double>> paymentsPerFee = getPaymentsPerMemberAndFee(collectivityId, from, to);

            List<CollectivityLocalStatistics> result = new ArrayList<>();
            for (Member m : members) {
                MemberDescription md = new MemberDescription();
                md.setId(m.getId());
                md.setFirstName(m.getFirstName());
                md.setLastName(m.getLastName());
                md.setEmail(m.getEmail());
                md.setOccupation(m.getOccupation());

                double potentialUnpaid = 0.0;
                for (MembershipFee fee : activeFees) {
                    double expected = calculateExpectedAmount(fee, from, to);
                    double paid = paymentsPerFee
                            .getOrDefault(m.getId(), Collections.emptyMap())
                            .getOrDefault(fee.getId(), 0.0);
                    double unpaid = expected - paid;
                    if (unpaid > 0) potentialUnpaid += unpaid;
                }

                double rate = attendanceRepository.computeAttendanceRate(
                        m.getId(), collectivityId, from, to, activityRepository);

                CollectivityLocalStatistics stat = new CollectivityLocalStatistics();
                stat.setMemberDescription(md);
                stat.setEarnedAmount(collectedPerMember.getOrDefault(m.getId(), 0.0));
                stat.setUnpaidAmount(potentialUnpaid);
                stat.setAttendanceRate(rate);
                result.add(stat);
            }
            return result;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public List<CollectivityOverallStatistics> getAllGlobalStatistics(LocalDate from, LocalDate to) {
        if (from == null || to == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both from and to dates are required");
        if (from.isAfter(to))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from date cannot be after to date");
        try {
            List<String> ids = getAllCollectivityIds();
            List<CollectivityOverallStatistics> result = new ArrayList<>();
            for (String id : ids) result.add(getGlobalStatistic(id, from, to));
            return result;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public CollectivityOverallStatistics getGlobalStatistic(String collectivityId,
                                                            LocalDate from,
                                                            LocalDate to) {
        validateCollectivityAndDates(collectivityId, from, to);
        try {
            List<CollectivityLocalStatistics> memberStats = getMemberStatistics(collectivityId, from, to);
            int total = memberStats.size();
            int upToDate = 0;
            double totalRate = 0;

            for (CollectivityLocalStatistics ms : memberStats) {
                if (ms.getUnpaidAmount() <= 0) upToDate++;
                if (ms.getAttendanceRate() != null) totalRate += ms.getAttendanceRate();
            }

            double percentage   = total > 0 ? (double) upToDate / total * 100 : 0.0;
            double globalRate   = total > 0 ? totalRate / total : 0.0;
            int newMembers      = getNewMembersCount(collectivityId, from, to);
            Collectivity col    = collectivityRepository.findById(collectivityId).orElse(null);

            CollectivityOverallStatistics stat = new CollectivityOverallStatistics();
            if (col != null) {
                CollectivityInformation info = new CollectivityInformation();
                info.setName(col.getName());
                info.setNumber(col.getNumber());
                stat.setCollectivityInformation(info);
            }
            stat.setOverallMemberCurrentDuePercentage(percentage);
            stat.setNewMembersNumber(newMembers);
            stat.setGlobalAttendanceRate(globalRate);
            return stat;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void validateCollectivityAndDates(String collectivityId, LocalDate from, LocalDate to) {
        try {
            if (!collectivityRepository.existsById(collectivityId))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collectivity not found");
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        if (from == null || to == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both from and to dates are required");
        if (from.isAfter(to))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from date cannot be after to date");
    }

    private List<MembershipFee> getActiveFees(String collectivityId) throws SQLException {
        List<MembershipFee> all = feeRepository.findByCollectivityId(collectivityId);
        List<MembershipFee> active = new ArrayList<>();
        for (MembershipFee f : all) {
            if (f.getStatus() == ActivityStatus.ACTIVE) active.add(f);
        }
        return active;
    }

    private double calculateExpectedAmount(MembershipFee fee, LocalDate from, LocalDate to) {
        int count = 0;
        LocalDate current = fee.getEligibleFrom();
        while (!current.isAfter(to)) {
            if (!current.isBefore(from)) count++;
            if (fee.getFrequency() == Frequency.PUNCTUALLY) break;
            else if (fee.getFrequency() == Frequency.MONTHLY)  current = current.plusMonths(1);
            else if (fee.getFrequency() == Frequency.ANNUALLY) current = current.plusYears(1);
            else break;
        }
        return count * fee.getAmount();
    }

    private Map<String, Double> getCollectedPerMember(String collectivityId,
                                                      LocalDate from,
                                                      LocalDate to) throws SQLException {
        String sql = """
            SELECT mp.member_id, SUM(mp.amount) as total
            FROM member_payments mp
            JOIN membership_fees mf ON mp.membership_fee_id = mf.id
            WHERE mf.collectivity_id = ?
              AND mp.creation_date >= ? AND mp.creation_date <= ?
            GROUP BY mp.member_id
            """;
        Map<String, Double> map = new HashMap<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("member_id"), rs.getDouble("total"));
        }
        return map;
    }

    private Map<String, Map<String, Double>> getPaymentsPerMemberAndFee(String collectivityId,
                                                                        LocalDate from,
                                                                        LocalDate to) throws SQLException {
        String sql = """
            SELECT mp.member_id, mp.membership_fee_id, SUM(mp.amount) as total
            FROM member_payments mp
            JOIN membership_fees mf ON mp.membership_fee_id = mf.id
            WHERE mf.collectivity_id = ?
              AND mp.creation_date >= ? AND mp.creation_date <= ?
            GROUP BY mp.member_id, mp.membership_fee_id
            """;
        Map<String, Map<String, Double>> map = new HashMap<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.computeIfAbsent(rs.getString("member_id"), k -> new HashMap<>())
                        .put(rs.getString("membership_fee_id"), rs.getDouble("total"));
            }
        }
        return map;
    }

    private int getNewMembersCount(String collectivityId,
                                   LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM members
            WHERE collectivity_id = ? AND joined_at >= ? AND joined_at <= ?
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private List<String> getAllCollectivityIds() throws SQLException {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT id FROM collectivities";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getString("id"));
        }
        return ids;
    }
}