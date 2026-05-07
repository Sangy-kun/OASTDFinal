package com.hei.TDOASFinal.repository;

import com.hei.TDOASFinal.config.DatabaseConnection;
import com.hei.TDOASFinal.model.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MembershipFeeRepository {

    public List<MembershipFee> findByCollectivityId(String collectivityId) throws SQLException {
        List<MembershipFee> list = new ArrayList<>();
        String sql = "SELECT * FROM membership_fees WHERE collectivity_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public MembershipFee save(String collectivityId, MembershipFee fee) throws SQLException {
        String sql = "INSERT INTO membership_fees (id, collectivity_id, eligible_from, frequency, amount, label, status) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fee.getId());
            ps.setString(2, collectivityId);
            ps.setDate(3, Date.valueOf(fee.getEligibleFrom()));
            ps.setString(4, fee.getFrequency().name());
            ps.setDouble(5, fee.getAmount());
            ps.setString(6, fee.getLabel());
            ps.setString(7, fee.getStatus().name());
            ps.executeUpdate();
        }
        return fee;
    }
    
    public Optional<MembershipFee> findById(String id) throws SQLException {
        String sql = "SELECT * FROM membership_fees WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    private MembershipFee map(ResultSet rs) throws SQLException {
        MembershipFee f = new MembershipFee();
        f.setId(rs.getString("id"));
        f.setCollectivityId(rs.getString("collectivity_id"));
        f.setEligibleFrom(rs.getDate("eligible_from").toLocalDate());
        f.setFrequency(Frequency.valueOf(rs.getString("frequency")));
        f.setAmount(rs.getDouble("amount"));
        f.setLabel(rs.getString("label"));
        f.setStatus(ActivityStatus.valueOf(rs.getString("status")));
        return f;
    }
}
