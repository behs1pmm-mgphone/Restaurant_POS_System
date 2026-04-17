package com.spring.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.spring.model.OrderItem;
import com.spring.repository.OrderItemRepository;

@Service
public class OrderService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private OrderItemRepository orderItemRepository;

    // ၁။ Order အသစ်လုပ်ခြင်း
    public int createNewOrder(int tableId, int userId) {
        String sql = "INSERT INTO `order` (restaurant_table_id, user_id, created_by, order_date, order_type, status, tax, service_charge, total_amount) " +
                     "VALUES (?, ?, ?, NOW(), 'Dine-in', 'Pending', 0.00, 0.00, 0.00)";

        jdbcTemplate.update(sql, tableId, userId, userId);
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

    // ၂။ မှာထားတဲ့ Items တွေကို သိမ်းခြင်း
    public void processAndSaveOrder(Map<String, String[]> paramMap, int orderId) {
        int i = 0;
        double grandTotal = 0;

        while (paramMap.containsKey("items[" + i + "].menuItemId")) {
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setMenuItemId(Integer.parseInt(paramMap.get("items[" + i + "].menuItemId")[0]));
            item.setQuantity(Integer.parseInt(paramMap.get("items[" + i + "].quantity")[0]));
            item.setNote(paramMap.get("items[" + i + "].note")[0]);

            double unitPrice = Double.parseDouble(paramMap.get("items[" + i + "].unitPrice")[0]);
            double total = Double.parseDouble(paramMap.get("items[" + i + "].total")[0]);

            item.setUnitPrice(unitPrice);
            item.setTotal(total);
            grandTotal += total;

            orderItemRepository.save(item);
            i++;
        }

        // Order Table မှာ စုစုပေါင်း ကျသင့်ငွေကို Update ပြန်လုပ်ပေးခြင်း
        updateOrderTotal(orderId, grandTotal);
    }

    // ၃။ (ဖြည့်စွက်ရန်) Waiter တစ်ယောက်ချင်းစီရဲ့ Order History ကို Items တွေနဲ့တကွ ဆွဲထုတ်ခြင်း
    public List<Map<String, Object>> getMyOrdersWithItems(int userId) {
        String sql = "SELECT o.*, " +
                     "GROUP_CONCAT(mi.name SEPARATOR ', ') as item_names, " +
                     "SUM(oi.quantity) as item_count " +
                     "FROM `order` o " +
                     "LEFT JOIN order_item oi ON o.order_id = oi.order_id " +
                     "LEFT JOIN menu_item mi ON oi.menu_item_id = mi.menu_item_id " +
                     "WHERE o.created_by = ? " +
                     "GROUP BY o.order_id " +
                     "ORDER BY o.order_date DESC";

        return jdbcTemplate.queryForList(sql, userId);
    }

    private void updateOrderTotal(int orderId, double total) {
        String sql = "UPDATE `order` SET total_amount = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, total, orderId);
    }
}