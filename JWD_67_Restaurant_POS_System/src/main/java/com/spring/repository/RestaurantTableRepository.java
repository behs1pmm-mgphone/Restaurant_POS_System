package com.spring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.spring.model.RestaurantTable;

@Repository
public class RestaurantTableRepository {
    private final JdbcTemplate jdbcTemplate;
    public RestaurantTableRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public long countTables() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM restaurant_table", Long.class);
    }

    public boolean existsByTableNumber(String tableNumber) {
        String sql = "SELECT COUNT(*) FROM restaurant_table WHERE table_number = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableNumber);
        return count != null && count > 0;
    }

    public int saveTable(RestaurantTable t, Integer adminId) {
        // 1. table_number, status, area_id ကို model မှယူသည်
        // 2. is_deleted ကို 0 (active) အဖြစ် သတ်မှတ်သည်
        // 3. created_at ကို NOW() (လက်ရှိအချိန်) သတ်မှတ်သည်
        // 4. created_by ကို login ဝင်ထားသော adminId ထည့်သွင်းသည်

        String sql = "INSERT INTO restaurant_table (table_number, status, area_id, is_deleted, created_at, created_by) " +
                     "VALUES (?, ?, ?, 0, NOW(), ?)";

        return jdbcTemplate.update(sql,
                t.getTable_number(),
                t.getStatus(),
                t.getArea_id(),
                adminId);
    }

    public int updateTableStatus(Integer id, String status) {
        return jdbcTemplate.update("UPDATE restaurant_table SET status = ? WHERE restaurant_table_id = ?", status, id);
    }

    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM restaurant_table WHERE status = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, status);
    }
    public List<RestaurantTable> findByAreaAreaId(Integer areaId) {

        String sql = "SELECT * FROM restaurant_table WHERE area_id = ? AND COALESCE(is_deleted, 0) = 0";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            RestaurantTable t = new RestaurantTable();
            t.setRestaurant_table_id(rs.getInt("restaurant_table_id"));
            t.setTable_number(rs.getString("table_number"));
            t.setStatus(rs.getString("status"));
            t.setArea_id(rs.getInt("area_id"));
            return t;
        }, areaId);
    }

    public Optional<RestaurantTable> findById(Integer id) {
        String sql = "SELECT * FROM restaurant_table WHERE restaurant_table_id = ?";
        try {
            RestaurantTable table = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                RestaurantTable t = new RestaurantTable();
                t.setRestaurant_table_id(rs.getInt("restaurant_table_id"));
                t.setTable_number(rs.getString("table_number"));
                t.setStatus(rs.getString("status"));
                t.setArea_id(rs.getInt("area_id"));
                return t;
            }, id);
            return Optional.ofNullable(table);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<RestaurantTable> findTablesByArea(Integer areaId) {

        String sql = "SELECT * FROM restaurant_table WHERE area_id = ? AND is_deleted = 0";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            RestaurantTable t = new RestaurantTable();
            t.setRestaurant_table_id(rs.getInt("restaurant_table_id"));
            t.setTable_number(rs.getString("table_number"));
            t.setStatus(rs.getString("status"));
            t.setArea_id(rs.getInt("area_id"));
            return t;
        }, areaId);
    }

    public int softDeleteTable(Integer tableId, Integer adminId) {
        String sql = "UPDATE restaurant_table " +
                     "SET is_deleted = 1, " +
                     "    deleted_at = NOW(), " +
                     "    deleted_by = ?, " +
                     "    status = 'Disabled' " +
                     "WHERE restaurant_table_id = ? AND is_deleted = 0";

        return jdbcTemplate.update(sql, adminId, tableId);
    }

}