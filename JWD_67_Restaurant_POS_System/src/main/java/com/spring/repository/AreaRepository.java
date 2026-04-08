package com.spring.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.spring.model.Area;

@Repository
public class AreaRepository {

    private final JdbcTemplate jdbcTemplate;

    public AreaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Area> findAllAreas() {
        String sql = "SELECT area_id, area_name, status FROM area WHERE is_deleted = 0";

        RowMapper<Area> rowMapper = (rs, rowNum) -> {
            Area area = new Area();
            area.setAreaId(rs.getInt("area_id"));
            area.setAreaName(rs.getString("area_name"));
            area.setStatus(rs.getString("status"));
            return area;
        };

        try {
            return jdbcTemplate.query(sql, rowMapper);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return List.of();
        }
    }

    public void save(Area area, Integer adminId) {
        // ✅ SAFETY: Ensure status is short enough if your DB has a small limit
        // 'Disabled' is 8 chars. If your DB column is VARCHAR(5), this is the crash.
        String status = (area.getStatus() == null || area.getStatus().trim().isEmpty()) ? "Active" : area.getStatus().trim();

        try {
            if (area.getAreaId() == null) {
                String sql = "INSERT INTO area (area_name, status, created_by, created_at, is_deleted) VALUES (?, ?, ?, NOW(), 0)";
                jdbcTemplate.update(sql, area.getAreaName(), status, adminId);
            } else {
                // ✅ This is the query you asked to fix.
                // It only updates the AREA table in Java, but triggers the RESTAURANT_TABLE in MySQL.
                String sql = "UPDATE area SET area_name = ?, status = ? WHERE area_id = ?";
                jdbcTemplate.update(sql, area.getAreaName(), status, area.getAreaId());
            }
        } catch (Exception e) {
            // Logs the exact reason why the SQL failed
            System.err.println("Database Error: " + e.getMessage());
            throw e;
        }
    }

    public void deleteById(Integer id, Integer adminId) {
        String sql = "UPDATE area SET is_deleted = 1, deleted_by = ?, deleted_at = NOW() WHERE area_id = ?";
        jdbcTemplate.update(sql, adminId, id);
    }

    public int areaSoftDelete(int areaId, int userId) {
        // I added spaces at the end of strings to prevent "SETis_deleted" errors
        String sql = "UPDATE area " +
                     "SET is_deleted = 1, " +
                     "deleted_by = ?, " +
                     "deleted_at = CURRENT_TIMESTAMP, " +
                     "status = 'INACTIVE' " +
                     "WHERE area_id = ? AND is_deleted = 0";

        return jdbcTemplate.update(sql, userId, areaId);
    }
}