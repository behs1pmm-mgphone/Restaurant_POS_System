package com.spring.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MenuItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public MenuItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- NEW: PAGINATION & SEARCH METHOD ---
    public List<Map<String, Object>> findMenuItems(Integer catId, String search, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
            "SELECT m.*, c.category_name FROM menu_item m " +
            "JOIN category c ON m.category_id = c.category_id " +
            "WHERE m.is_deleted = 0 "
        );
        List<Object> params = new ArrayList<>();

        if (catId != null && catId > 0) {
            sql.append("AND m.category_id = ? ");
            params.add(catId);
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND m.name LIKE ? ");
            params.add("%" + search.trim() + "%");
        }

        // Ordering by newest first and applying limit/offset for pagination
        sql.append("ORDER BY m.menu_item_id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    // --- NEW: TOTAL COUNT FOR PAGINATION ---
    public long countMenuItems(Integer catId, String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM menu_item WHERE is_deleted = 0 ");
        List<Object> params = new ArrayList<>();

        if (catId != null && catId > 0) {
            sql.append("AND category_id = ? ");
            params.add(catId);
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND name LIKE ? ");
            params.add("%" + search.trim() + "%");
        }

        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }

    // --- ORIGINAL METHODS (Kept exactly as requested) ---

    public Map<String, Object> findById(int id) {
        String sql = "SELECT * FROM menu_item WHERE menu_item_id = ? AND is_deleted = 0";
        return jdbcTemplate.queryForMap(sql, id);
    }

    public int save(String name, double price, int stock, int catId, String status, String img, int adminId) {
        String sql = "INSERT INTO menu_item (name, price, stock_quantity, category_id, status, image, " +
                     "created_at, created_by, is_deleted) VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, 0)";
        return jdbcTemplate.update(sql, name, price, stock, catId, status, img, adminId);
    }

    public int update(int id, String name, double price, int stock, int catId, String status, String img, int adminId) {
        String sql = "UPDATE menu_item SET name=?, price=?, stock_quantity=?, category_id=?, " +
                     "status=?, image=?, updated_at=CURDATE(), updated_by=? WHERE menu_item_id=?";
        return jdbcTemplate.update(sql, name, price, stock, catId, status, img, adminId, id);
    }

    public int softDelete(int id, int adminId) {
        String sql = "UPDATE menu_item SET is_deleted = 1, deleted_at = NOW(), deleted_by = ? WHERE menu_item_id = ?";
        return jdbcTemplate.update(sql, adminId, id);
    }

    public long countAllMenuItems() {
        String sql = "SELECT COUNT(*) FROM menu_item WHERE is_deleted = 0";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}