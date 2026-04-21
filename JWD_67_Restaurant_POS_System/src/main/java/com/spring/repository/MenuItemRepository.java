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
            "SELECT m.*, c.category_name, " +
            "u1.user_name AS created_by_name, " + // Created လုပ်တဲ့သူ့နာမည်
            "u2.user_name AS updated_by_name " + // Edit လုပ်တဲ့သူ့နာမည်
            "FROM menu_item m " +
            "JOIN category c ON m.category_id = c.category_id " +
            "LEFT JOIN user u1 ON m.created_by = u1.user_id " + // User table နဲ့ ပထမအကြိမ် Join
            "LEFT JOIN user u2 ON m.updated_by = u2.user_id " + // User table နဲ့ ဒုတိယအကြိမ် Join
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

        sql.append("ORDER BY m.menu_item_id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }
    
    // ၂။ စုစုပေါင်း Item အရေအတွက်ကို တွက်ပေးသည့် Method (Pagination တွက်ချက်ရန် လိုအပ်သည်)
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

	/*
	 * // --- NEW: TOTAL COUNT FOR PAGINATION --- public long countMenuItems(Integer
	 * catId, String search) { StringBuilder sql = new
	 * StringBuilder("SELECT COUNT(*) FROM menu_item WHERE is_deleted = 0 ");
	 * List<Object> params = new ArrayList<>();
	 * 
	 * if (catId != null && catId > 0) { sql.append("AND category_id = ? ");
	 * params.add(catId); } if (search != null && !search.trim().isEmpty()) {
	 * sql.append("AND name LIKE ? "); params.add("%" + search.trim() + "%"); }
	 * 
	 * return jdbcTemplate.queryForObject(sql.toString(), Long.class,
	 * params.toArray()); }
	 */

    // --- ORIGINAL METHODS (Kept exactly as requested) ---

    public Map<String, Object> findById(int id) {
        String sql = "SELECT * FROM menu_item WHERE menu_item_id = ? AND is_deleted = 0";
        return jdbcTemplate.queryForMap(sql, id);
    }
    
    public boolean isNameExists(String name) {
        // is_deleted = 0 ဖြစ်နေတဲ့ item တွေထဲမှာပဲ နာမည်တူရှိမရှိ စစ်တာပါ
        String sql = "SELECT COUNT(*) FROM menu_item WHERE name = ? AND is_deleted = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name);
        return count != null && count > 0;
    }

    public int save(String name, double price, int catId, String status, String img, int adminId) {
        String sql = "INSERT INTO menu_item (name, price, category_id, status, image, created_at, created_by, is_deleted) " +
               "VALUES (?, ?, ?, ?, ?, NOW(), ?, 0)";
         // 1.name, 2.price, 3.catId, 4.status, 5.img, 6.adminId
  return jdbcTemplate.update(sql, name, price, catId, status, img, adminId);
}

    public int update(int id, String name, double price, int catId, String status, String img, int adminId) {
        String sql = "UPDATE menu_item SET name=?, price=?, category_id=?, " +
               "status=?, image=?, updated_at=NOW(), updated_by=? WHERE menu_item_id=?";
         // 1.name, 2.price, 3.catId, 4.status, 5.img, 6.adminId, 7.id (WHERE clause အတွက်)
  return jdbcTemplate.update(sql, name, price, catId, status, img, adminId, id);
}

    public int softDelete(int id, int adminId) {
        String sql = "UPDATE menu_item SET is_deleted = 1, deleted_at = NOW(), deleted_by = ? WHERE menu_item_id = ?";
        return jdbcTemplate.update(sql, adminId, id);
    }

    public long countAllMenuItems() {
        String sql = "SELECT COUNT(*) FROM menu_item WHERE is_deleted = 0";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
    
 // --- STATUS တစ်ခုတည်းကိုသာ Update လုပ်ရန် (Toggle Switch အတွက်) ---
    public int updateStatus(int id, String status, int adminId) {
        String sql = "UPDATE menu_item SET status = ?, updated_at = NOW(), updated_by = ? WHERE menu_item_id = ?";
        
        // 1. status ('Available' or 'Sold Out')
        // 2. adminId (ဘယ်သူပြင်သွားလဲ သိအောင်)
        // 3. id (ဘယ် item ကို ပြင်မှာလဲ)
        return jdbcTemplate.update(sql, status, adminId, id);
    }
    public List<Map<String, Object>> findAllStatuses() {
        // delete_flg = 0 ဖြစ်တဲ့ (မဖျက်ရသေးတဲ့) item အားလုံးရဲ့ id နဲ့ status ကို ယူတာပါ
        // menu_item_id ကို 'id' လို့ alias ပေးထားမှ JavaScript ဘက်က item.id ဆိုပြီး ဖတ်ရလွယ်မှာပါ
        String sql = "SELECT menu_item_id as id, status FROM menu_item WHERE delete_flg = 0";
        
        return jdbcTemplate.queryForList(sql);
    }
}