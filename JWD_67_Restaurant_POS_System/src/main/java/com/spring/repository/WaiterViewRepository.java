package com.spring.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.spring.model.WaiterView;

@Repository
public class WaiterViewRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Retrieves a paginated list of menu items.
     * For 19 items with a size of 8:
     * Page 1: Offset 0 (Items 1-8)
     * Page 2: Offset 8 (Items 9-16)
     * Page 3: Offset 16 (Items 17-19)
     */
    public List<WaiterView> findPaginated(int page, int size, String category, String search) {
        // Offset logic: (1-1)*8 = 0; (2-1)*8 = 8; (3-1)*8 = 16
        int offset = (page - 1) * size;

        StringBuilder sql = new StringBuilder("SELECT menu_item_id, name, price, category_id, image, status " +
                                              "FROM menu_item WHERE (is_deleted = 0 OR is_deleted IS NULL)");
        List<Object> params = new ArrayList<>();

        // Category Filter logic
        if (category != null && !category.equalsIgnoreCase("ALL")) {
            sql.append(" AND category_id = ?");
            params.add(category);
        }

        // Search Filter logic
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.add("%" + search.toLowerCase().trim() + "%");
        }

        // Sorting by ID ensures that as you click 'Next', the order stays consistent
        sql.append(" ORDER BY menu_item_id ASC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            WaiterView item = new WaiterView();
            item.setId(rs.getInt("menu_item_id"));
            item.setName(rs.getString("name"));
            item.setPrice(rs.getDouble("price"));

            // This retrieves the filename (e.g., 'burger.jpg')
            item.setImage(rs.getString("image"));

            item.setCategory(String.valueOf(rs.getInt("category_id")));

            // Availability Logic (Integer အဖြစ်သို့ ပြောင်းလဲခြင်း)
            String status = rs.getString("status");
            boolean isAvailable = (status != null && status.trim().equalsIgnoreCase("Available"));
            
            // model ထဲက setStockQuantity(Integer) နဲ့ ကိုက်ညီအောင် 1 သို့မဟုတ် 0 ထည့်ပေးပါ
            item.setStockQuantity(isAvailable ? 1 : 0);

            return item;
        }, params.toArray());
    }

    public int countItems(String category, String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM menu_item WHERE (is_deleted = 0 OR is_deleted IS NULL)");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.equalsIgnoreCase("ALL")) {
            sql.append(" AND category_id = ?");
            params.add(category);
        }

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.add("%" + search.toLowerCase().trim() + "%");
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return (count != null) ? count : 0;
    }

    public void updateStockStatus(int id, String status) {
        String sql = "UPDATE menu_item SET status = ? WHERE menu_item_id = ?";
        jdbcTemplate.update(sql, status, id);
    }

    public void updateTableStatus(int tableId, String status) {
        String sql = "UPDATE restaurant_tables SET status = ? WHERE table_id = ?";
        jdbcTemplate.update(sql, status, tableId);
    }
}