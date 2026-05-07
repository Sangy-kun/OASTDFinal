package com.hei.TDOASFinal.repository;

import com.hei.TDOASFinal.config.DatabaseConnection;
import com.hei.TDOASFinal.model.Activity;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public class ActivityRepository {

    public Activity save(String collectivityId, Activity a) throws SQLException {
        a.setId(UUID.randomUUID().toString());
        a.setCollectivityId(collectivityId);
        String sql = """
            INSERT INTO activities (id, collectivity_id, title, type, activity_date, is_mandatory)
            VALUES (?,?,?,?,?,?)
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getId());
            ps.setString(2, a.getCollectivityId());
            ps.setString(3, a.getTitle());
            ps.setString(4, a.getType());
            ps.setDate(5, Date.valueOf(a.getActivityDate()));
            ps.setBoolean(6, Boolean.TRUE.equals(a.getIsMandatory()));
            ps.executeUpdate();
        }
        return a;
    }

    public List<Activity> findByCollectivityId(String collectivityId) throws SQLException {
        List<Activity> list = new ArrayList<>();
        String sql = "SELECT * FROM activities WHERE collectivity_id = ? ORDER BY activity_date";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<Activity> findById(String id) throws SQLException {
        String sql = "SELECT * FROM activities WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    public List<Activity> findMandatoryBetween(String collectivityId,
                                               LocalDate from,
                                               LocalDate to) throws SQLException {
        List<Activity> list = new ArrayList<>();
        String sql = """
            SELECT * FROM activities
            WHERE collectivity_id = ?
              AND is_mandatory = TRUE
              AND activity_date >= ?
              AND activity_date <= ?
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Activity map(ResultSet rs) throws SQLException {
        Activity a = new Activity();
        a.setId(rs.getString("id"));
        a.setCollectivityId(rs.getString("collectivity_id"));
        a.setTitle(rs.getString("title"));
        a.setType(rs.getString("type"));
        a.setActivityDate(rs.getDate("activity_date").toLocalDate());
        a.setIsMandatory(rs.getBoolean("is_mandatory"));
        return a;
    }
}