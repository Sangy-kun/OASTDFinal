package com.hei.TDOASFinal.service;

import com.hei.TDOASFinal.config.DatabaseConnection;
import com.hei.TDOASFinal.model.*;
import com.hei.TDOASFinal.repository.CollectivityRepository;
import com.hei.TDOASFinal.repository.MemberRepository;
import com.hei.TDOASFinal.repository.MembershipFeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Service
public class StatisticService {

    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;
    private final MembershipFeeRepository feeRepository;

    public StatisticService(CollectivityRepository collectivityRepository,
                            MemberRepository memberRepository,
                            MembershipFeeRepository feeRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
        this.feeRepository = feeRepository;
    }

    public List<CollectivityLocalStatistics> getMemberStatistics(String collectivityId, LocalDate from, LocalDate to) {
        validateCollectivityAndDates(collectivityId, from, to);

        try {
            List<Member> members = memberRepository.findByCollectivityId(collectivityId);
            List<MembershipFee> activeFees = getActiveFees(collectivityId);

            Map<String, Double> collectedPerMember = getCollectedPerMember(collectivityId, from, to);
            Map<String, Map<String, Double>> paymentsPerFee = getPaymentsPerMemberAndFee(collectivityId, from, to);

            List<CollectivityLocalStatistics> result = new ArrayList<>();

            for (Member m : members) {
                CollectivityLocalStatistics stat = new CollectivityLocalStatistics();
                
                MemberDescription md = new MemberDescription();
                md.setId(m.getId());
                md.setFirstName(m.getFirstName());
                md.setLastName(m.getLastName());
                md.setEmail(m.getEmail());
                md.setOccupation(m.getOccupation());
                stat.setMemberDescription(md);
                
                stat.setEarnedAmount(collectedPerMember.getOrDefault(m.getId(), 0.0));

                double potentialUnpaid = 0.0;
                for (MembershipFee fee : activeFees) {
                    double expected = calculateExpectedAmount(fee, from, to);
                    double paid = paymentsPerFee.getOrDefault(m.getId(), Collections.emptyMap())
                                                .getOrDefault(fee.getId(), 0.0);
                    double unpaid = expected - paid;
                    if (unpaid > 0) {
                        potentialUnpaid += unpaid;
                    }
                }
                stat.setUnpaidAmount(potentialUnpaid);
                result.add(stat);
            }

            return result;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public List<CollectivityOverallStatistics> getAllGlobalStatistics(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from date cannot be after to date");
        }

        try {
            List<String> collectivityIds = getAllCollectivityIds();
            List<CollectivityOverallStatistics> result = new ArrayList<>();
            for (String cid : collectivityIds) {
                result.add(buildOverallStat(cid, from, to));
            }
            return result;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    private CollectivityOverallStatistics buildOverallStat(String collectivityId, LocalDate from, LocalDate to) throws SQLException {
        List<Member> members = memberRepository.findByCollectivityId(collectivityId);
        List<MembershipFee> activeFees = getActiveFees(collectivityId);

        Map<String, Map<String, Double>> paymentsPerFee = getPaymentsPerMemberAndFee(collectivityId, from, to);

        int totalMembers = members.size();
        int upToDateCount = 0;

        for (Member m : members) {
            double potentialUnpaid = 0.0;
            for (MembershipFee fee : activeFees) {
                double expected = calculateExpectedAmount(fee, from, to);
                double paid = paymentsPerFee
                        .getOrDefault(m.getId(), Collections.emptyMap())
                        .getOrDefault(fee.getId(), 0.0);
                double unpaid = expected - paid;
                if (unpaid > 0) potentialUnpaid += unpaid;
            }
            if (potentialUnpaid <= 0) upToDateCount++;
        }

        double percentage = totalMembers > 0 ? ((double) upToDateCount / totalMembers) * 100.0 : 0.0;
        int newMembers = getNewMembersCount(collectivityId, from, to);

        CollectivityOverallStatistics stat = new CollectivityOverallStatistics();
        // Fetch name/number directly without triggering buildFull (avoids NPE on null bureau), i guess, it worked, so won't touch it anymore
        CollectivityInformation info = getCollectivityInformation(collectivityId);
        stat.setCollectivityInformation(info);
        stat.setOverallMemberCurrentDuePercentage(percentage);
        stat.setNewMembersNumber(newMembers);
        return stat;
    }

    public CollectivityOverallStatistics getGlobalStatistic(String collectivityId, LocalDate from, LocalDate to) {
        validateCollectivityAndDates(collectivityId, from, to);
        try {
            return buildOverallStat(collectivityId, from, to);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void validateCollectivityAndDates(String collectivityId, LocalDate from, LocalDate to) {
        try {
            if (!collectivityRepository.existsById(collectivityId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collectivity not found");
            }
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from date cannot be after to date");
        }
    }

    private List<MembershipFee> getActiveFees(String collectivityId) throws SQLException {
        List<MembershipFee> all = feeRepository.findByCollectivityId(collectivityId);
        List<MembershipFee> active = new ArrayList<>();
        for (MembershipFee f : all) {
            if (f.getStatus() == ActivityStatus.ACTIVE) {
                active.add(f);
            }
        }
        return active;
    }

    private double calculateExpectedAmount(MembershipFee fee, LocalDate from, LocalDate to) {
        int count = 0;
        LocalDate current = fee.getEligibleFrom();
        
        while (!current.isAfter(to)) {
            if (!current.isBefore(from)) {
                count++;
            }
            if (fee.getFrequency() == Frequency.PUNCTUALLY) {
                break;
            } else if (fee.getFrequency() == Frequency.WEEKLY) {
                current = current.plusWeeks(1);
            } else if (fee.getFrequency() == Frequency.MONTHLY) {
                current = current.plusMonths(1);
            } else if (fee.getFrequency() == Frequency.ANNUALLY) {
                current = current.plusYears(1);
            } else {
                break;
            }
        }
        return count * fee.getAmount();
    }

    private Map<String, Double> getCollectedPerMember(String collectivityId, LocalDate from, LocalDate to) throws SQLException {
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
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("member_id"), rs.getDouble("total"));
            }
        }
        return map;
    }

    private Map<String, Map<String, Double>> getPaymentsPerMemberAndFee(String collectivityId, LocalDate from, LocalDate to) throws SQLException {
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
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String memberId = rs.getString("member_id");
                String feeId = rs.getString("membership_fee_id");
                double amount = rs.getDouble("total");
                map.computeIfAbsent(memberId, k -> new HashMap<>()).put(feeId, amount);
            }
        }
        return map;
    }

    private int getNewMembersCount(String collectivityId, LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM members
            WHERE collectivity_id = ?
              AND joined_at >= ? AND joined_at <= ?
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private CollectivityInformation getCollectivityInformation(String collectivityId) throws SQLException {
        String sql = "SELECT name, number FROM collectivities WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                CollectivityInformation info = new CollectivityInformation();
                info.setName(rs.getString("name"));
                Object numObj = rs.getObject("number");
                info.setNumber(numObj != null ? ((Number) numObj).intValue() : null);
                return info;
            }
        }
        return null;
    }

    private List<String> getAllCollectivityIds() throws SQLException {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT id FROM collectivities";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        }
        return ids;
    }
}
