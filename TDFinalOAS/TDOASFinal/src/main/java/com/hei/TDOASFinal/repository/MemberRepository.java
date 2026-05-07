package com.hei.TDOASFinal.repository;

import com.hei.TDOASFinal.config.DatabaseConnection;
import com.hei.TDOASFinal.model.Member;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MemberRepository {

    public Member save(Member m) throws SQLException {
        m.setId(UUID.randomUUID().toString());
        String sql = """
            INSERT INTO members
              (id, first_name, last_name, birth_date, gender,
               address, profession, phone_number, email, occupation, collectivity_id)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getId());
            ps.setString(2, m.getFirstName());
            ps.setString(3, m.getLastName());
            ps.setDate(4, Date.valueOf(m.getBirthDate()));
            ps.setString(5, m.getGender());
            ps.setString(6, m.getAddress());
            ps.setString(7, m.getProfession());
            ps.setLong(8, m.getPhoneNumber());
            ps.setString(9, m.getEmail());
            ps.setString(10, m.getOccupation());
            ps.setString(11, null);
            ps.executeUpdate();
        }
        if (m.getReferees() != null && !m.getReferees().isEmpty()) {
            saveReferees(m.getId(), m.getReferees());
        }
        return m;
    }

    private void saveReferees(String memberId, List<Member> referees) throws SQLException {
            String sql = "INSERT INTO member_referees (member_id, referee_id) VALUES (?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (Member ref : referees) {
                ps.setString(1, memberId);
                ps.setString(2, ref.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void updateCollectivityId(String memberId, String collectivityId) throws SQLException {
        String sql = "UPDATE members SET collectivity_id = ? WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ps.setString(2, memberId);
            ps.executeUpdate();
        }
    }

    public Optional<Member> findById(String id) throws SQLException {
        String sql = "SELECT * FROM members WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    public List<Member> findByCollectivityId(String collectivityId) throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE collectivity_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Member> findRefereesByMemberId(String memberId) throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = """
            SELECT m.* FROM members m
            JOIN member_referees mr ON m.id = mr.referee_id
            WHERE mr.member_id = ?
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, memberId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public boolean existsById(String id) throws SQLException {
        String sql = "SELECT 1 FROM members WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeQuery().next();
        }
    }

    private Member map(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setId(rs.getString("id"));
        m.setFirstName(rs.getString("first_name"));
        m.setLastName(rs.getString("last_name"));
        m.setBirthDate(rs.getDate("birth_date").toLocalDate().toString());
        m.setGender(rs.getString("gender"));
        m.setAddress(rs.getString("address"));
        m.setProfession(rs.getString("profession"));
        m.setPhoneNumber(rs.getLong("phone_number"));
        m.setEmail(rs.getString("email"));
        m.setOccupation(rs.getString("occupation"));
        m.setCollectivityId(rs.getString("collectivity_id"));
        return m;
    }

}