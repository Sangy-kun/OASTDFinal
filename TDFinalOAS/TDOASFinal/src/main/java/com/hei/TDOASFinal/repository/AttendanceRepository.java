package com.hei.TDOASFinal.repository;

import com.hei.TDOASFinal.config.DatabaseConnection;
import com.hei.TDOASFinal.model.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class AttendanceRepository {

    private final MemberRepository memberRepository;

    public AttendanceRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public boolean existsByActivityAndMember(String activityId,
                                             String memberId) throws SQLException {
        String sql = "SELECT 1 FROM attendance WHERE activity_id = ? AND member_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, activityId);
            ps.setString(2, memberId);
            return ps.executeQuery().next();
        }
    }

    public Attendance save(String activityId, CreateAttendance input) throws SQLException {
        String id = UUID.randomUUID().toString();
        String sql = """
            INSERT INTO attendance (id, activity_id, member_id, status, is_from_another_collectivity)
            VALUES (?,?,?,?,?)
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, activityId);
            ps.setString(3, input.getMemberId());
            ps.setString(4, input.getStatus().name());
            ps.setBoolean(5, Boolean.TRUE.equals(input.getIsFromAnotherCollectivity()));
            ps.executeUpdate();
        }
        Attendance att = new Attendance();
        att.setId(id);
        att.setActivityId(activityId);
        att.setStatus(input.getStatus());
        att.setIsFromAnotherCollectivity(Boolean.TRUE.equals(input.getIsFromAnotherCollectivity()));
        att.setMember(memberRepository.findById(input.getMemberId()).orElse(null));
        return att;
    }

    public List<Attendance> findByActivityId(String activityId) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE activity_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, activityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Attendance att = new Attendance();
                att.setId(rs.getString("id"));
                att.setActivityId(rs.getString("activity_id"));
                att.setStatus(AttendanceStatus.valueOf(rs.getString("status")));
                att.setIsFromAnotherCollectivity(rs.getBoolean("is_from_another_collectivity"));
                att.setMember(memberRepository.findById(rs.getString("member_id")).orElse(null));
                list.add(att);
            }
        }
        return list;
    }

    public double computeAttendanceRate(String memberId,
                                        String collectivityId,
                                        LocalDate from,
                                        LocalDate to,
                                        ActivityRepository activityRepository) throws SQLException {
        List<Activity> mandatory = activityRepository.findMandatoryBetween(collectivityId, from, to);
        if (mandatory.isEmpty()) return 100.0;

        int total = mandatory.size();
        int present = 0;

        for (Activity act : mandatory) {
            String sql = """
                SELECT status FROM attendance
                WHERE activity_id = ? AND member_id = ?
                  AND is_from_another_collectivity = FALSE
                """;
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, act.getId());
                ps.setString(2, memberId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String s = rs.getString("status");
                    if ("PRESENT".equals(s) || "EXCUSED".equals(s)) present++;
                }
            }
        }
        return (double) present / total * 100;
    }
}