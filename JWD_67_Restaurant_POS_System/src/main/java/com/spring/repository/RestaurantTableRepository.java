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

    public List<RestaurantTable> findTablesByArea(Integer areaId) {
        String sql = "SELECT * FROM restaurant_table WHERE area_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            RestaurantTable t = new RestaurantTable();
            t.setRestaurant_table_id(rs.getInt("restaurant_table_id"));
            t.setTable_number(rs.getString("table_number"));
            t.setStatus(rs.getString("status"));
            t.setArea_id(rs.getInt("area_id"));
            return t;
        }, areaId);
    }

    public boolean existsByTableNumber(String tableNumber) {
        String sql = "SELECT COUNT(*) FROM restaurant_table WHERE table_number = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableNumber);
        return count != null && count > 0;
    }

    public int saveTable(RestaurantTable t) {
        return jdbcTemplate.update("INSERT INTO restaurant_table (table_number, status, area_id) VALUES (?, ?, ?)",
                t.getTable_number(), t.getStatus(), t.getArea_id());
    }

    public int updateTableStatus(Integer id, String status) {
        return jdbcTemplate.update("UPDATE restaurant_table SET status = ? WHERE restaurant_table_id = ?", status, id);
    }

    public int deleteTable(Integer id) {
        return jdbcTemplate.update("DELETE FROM restaurant_table WHERE restaurant_table_id = ?", id);
    }

    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM restaurant_table WHERE status = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, status);
    }
    public List<RestaurantTable> findByAreaAreaId(Integer areaId) {
        String sql = "SELECT * FROM restaurant_table WHERE area_id = ?";
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
                return t;
            }, id);
            return Optional.ofNullable(table);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}