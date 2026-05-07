package com.hei.TDOASFinal.repository;

import com.hei.TDOASFinal.config.DatabaseConnection;
import com.hei.TDOASFinal.model.Collectivity;
import com.hei.TDOASFinal.model.CollectivityStructure;
import com.hei.TDOASFinal.model.Member;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class CollectivityRepository {

    private final MemberRepository memberRepository;

    public CollectivityRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Collectivity save(Collectivity col) throws SQLException {
        CollectivityStructure s = col.getStructure();
        String sql = """
            INSERT INTO collectivities
              (id, location, specialty, president_id, vice_president_id, treasurer_id, secretary_id)
            VALUES (?,?,?,?,?,?,?)
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, col.getId());
            ps.setString(2, col.getLocation());
            ps.setString(3, col.getSpecialty());
            ps.setString(4, s.getPresident().getId());
            ps.setString(5, s.getVicePresident().getId());
            ps.setString(6, s.getTreasurer().getId());
            ps.setString(7, s.getSecretary().getId());
            ps.executeUpdate();
        }
        if (col.getMembers() != null) {
            for (Member m : col.getMembers()) {
                memberRepository.updateCollectivityId(m.getId(), col.getId());
            }
        }
        return buildFull(col);
    }

    public Optional<Collectivity> findById(String id) throws SQLException {
        String sql = "SELECT * FROM collectivities WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Collectivity col = mapRow(rs);
                return Optional.of(buildFull(col));
            }
        }
        return Optional.empty();
    }

    public boolean existsById(String id) throws SQLException {
        String sql = "SELECT 1 FROM collectivities WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeQuery().next();
        }
    }

    public boolean existsByName(String name) throws SQLException {
        String sql = "SELECT 1 FROM collectivities WHERE name = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            return ps.executeQuery().next();
        }
    }

    public boolean numberExists(Integer number) throws SQLException {
        String sql = "SELECT 1 FROM collectivities WHERE number = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, number);
            return ps.executeQuery().next();
        }
    }

    public Collectivity updateInformation(String id, String name, Integer number) throws SQLException {
        String sql = "UPDATE collectivities SET number = ?, name = ? WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, number);
            ps.setString(2, name);
            ps.setString(3, id);
            ps.executeUpdate();
        }
        return findById(id).orElseThrow(() ->
                new SQLException("Collectivity not found after update: " + id));
    }

    private Collectivity buildFull(Collectivity col) throws SQLException {
        CollectivityStructure s = col.getStructure();
        s.setPresident(loadMember(s.getPresident().getId()));
        s.setVicePresident(loadMember(s.getVicePresident().getId()));
        s.setTreasurer(loadMember(s.getTreasurer().getId()));
        s.setSecretary(loadMember(s.getSecretary().getId()));
        List<Member> members = memberRepository.findByCollectivityId(col.getId());
        for (Member m : members) {
            m.setReferees(memberRepository.findRefereesByMemberId(m.getId()));
        }
        col.setMembers(members);
        return col;
    }

    private Collectivity mapRow(ResultSet rs) throws SQLException {
        Collectivity c = new Collectivity();
        c.setId(rs.getString("id"));
        Object numObj = rs.getObject("number");
        c.setNumber(numObj != null ? ((Number) numObj).intValue() : null);
        c.setName(rs.getString("name"));
        c.setLocation(rs.getString("location"));
        c.setSpecialty(rs.getString("specialty"));
        CollectivityStructure structure = new CollectivityStructure();
        Member president = new Member();
        president.setId(rs.getString("president_id"));
        Member vicePresident = new Member();
        vicePresident.setId(rs.getString("vice_president_id"));
        Member treasurer = new Member();
        treasurer.setId(rs.getString("treasurer_id"));
        Member secretary = new Member();
        secretary.setId(rs.getString("secretary_id"));
        structure.setPresident(president);
        structure.setVicePresident(vicePresident);
        structure.setTreasurer(treasurer);
        structure.setSecretary(secretary);
        c.setStructure(structure);
        return c;
    }

    private Member loadMember(String id) throws SQLException {
        return memberRepository.findById(id).orElse(null);
    }
}