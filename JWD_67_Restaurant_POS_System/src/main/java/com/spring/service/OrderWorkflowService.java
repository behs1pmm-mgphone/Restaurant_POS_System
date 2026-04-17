package com.spring.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderWorkflowService {

    private final JdbcTemplate jdbcTemplate;

    public OrderWorkflowService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getOrderItemsBoard() {
        String sql =
                "SELECT o.order_id, o.order_type, o.status AS order_status, " +
                "       t.table_number, " +
                "       oi.order_item_id, oi.item_status, oi.quantity, oi.unit_price, oi.total, oi.note, " +
                "       mi.name AS menu_name " +
                "FROM `order` o " +
                "JOIN restaurant_table t ON o.restaurant_table_id = t.restaurant_table_id " +
                "JOIN order_item oi ON o.order_id = oi.order_id " +
                "JOIN menu_item mi ON oi.menu_item_id = mi.menu_item_id " +
                "WHERE o.status = 'Pending' OR oi.item_status <> 'Paid' " +
                "ORDER BY o.order_id DESC, oi.order_item_id ASC";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getWaiterOrdersWithItems(int waiterId) {
        String sql =
                "SELECT o.order_id, o.order_date, o.order_type, o.status AS order_status, o.total_amount, " +
                "       t.table_number, " +
                "       oi.order_item_id, oi.menu_item_id, oi.quantity, oi.unit_price, oi.total, oi.note, oi.item_status, " +
                "       mi.name AS menu_name " +
                "FROM `order` o " +
                "JOIN restaurant_table t ON o.restaurant_table_id = t.restaurant_table_id " +
                "LEFT JOIN order_item oi ON o.order_id = oi.order_id " +
                "LEFT JOIN menu_item mi ON oi.menu_item_id = mi.menu_item_id " +
                "WHERE o.created_by = ? " +
                "ORDER BY o.order_id DESC, oi.order_item_id ASC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, waiterId);
        Map<Integer, Map<String, Object>> orderMap = new HashMap<>();

        for (Map<String, Object> row : rows) {
            Integer orderId = (Integer) row.get("order_id");
            Map<String, Object> order = orderMap.computeIfAbsent(orderId, id -> {
                Map<String, Object> m = new HashMap<>();
                m.put("order_id", row.get("order_id"));
                m.put("order_date", row.get("order_date"));
                m.put("order_type", row.get("order_type"));
                m.put("order_status", row.get("order_status"));
                m.put("table_number", row.get("table_number"));
                m.put("total_amount", row.get("total_amount"));
                m.put("items", new ArrayList<Map<String, Object>>());
                return m;
            });

            Object orderItemId = row.get("order_item_id");
            if (orderItemId != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
                Map<String, Object> item = new HashMap<>();
                item.put("order_item_id", row.get("order_item_id"));
                item.put("menu_item_id", row.get("menu_item_id"));
                item.put("menu_name", row.get("menu_name"));
                item.put("quantity", row.get("quantity"));
                item.put("unit_price", row.get("unit_price"));
                item.put("total", row.get("total"));
                item.put("note", row.get("note"));
                item.put("item_status", row.get("item_status"));
                items.add(item);
            }
        }

        return new ArrayList<>(orderMap.values());
    }

    public List<Map<String, Object>> getMenuOptions() {
        String sql =
                "SELECT menu_item_id, name, price " +
                "FROM menu_item " +
                "WHERE is_deleted = 0 AND status = 'Available' " +
                "ORDER BY name ASC";
        return jdbcTemplate.queryForList(sql);
    }

    public boolean addItemToWaiterOrder(int waiterId, int orderId, int menuItemId, int quantity, String note) {
        Integer owns = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `order` WHERE order_id = ? AND created_by = ? AND status = 'Pending'",
                Integer.class,
                orderId, waiterId
        );
        if (owns == null || owns == 0) {
            return false;
        }

        Map<String, Object> menu = jdbcTemplate.queryForMap(
                "SELECT price FROM menu_item WHERE menu_item_id = ? AND is_deleted = 0",
                menuItemId
        );
        Number priceNum = (Number) menu.get("price");
        if (priceNum == null) {
            return false;
        }
        double unitPrice = priceNum.doubleValue();
        double total = unitPrice * quantity;

        jdbcTemplate.update(
                "INSERT INTO order_item (order_id, menu_item_id, note, quantity, unit_price, total, item_status) VALUES (?, ?, ?, ?, ?, ?, 'Pending')",
                orderId, menuItemId, note == null ? "" : note.trim(), quantity, unitPrice, total
        );
        recalculateOrderTotal(orderId);
        return true;
    }

    public boolean deleteOrderItemByWaiter(int waiterId, int orderItemId) {
        String sql =
                "SELECT o.order_id, oi.item_status " +
                "FROM order_item oi " +
                "JOIN `order` o ON oi.order_id = o.order_id " +
                "WHERE oi.order_item_id = ? AND o.created_by = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, orderItemId, waiterId);
        if (rows.isEmpty()) {
            return false;
        }

        Integer orderId = (Integer) rows.get(0).get("order_id");
        String itemStatus = (String) rows.get(0).get("item_status");
        if ("Paid".equals(itemStatus)) {
            return false;
        }

        int deleted = jdbcTemplate.update("DELETE FROM order_item WHERE order_item_id = ?", orderItemId);
        if (deleted > 0 && orderId != null) {
            recalculateOrderTotal(orderId);
            syncOrderStatus(orderId);
            return true;
        }
        return false;
    }

    public boolean updateItemStatusByRole(int roleId, int orderItemId, String targetStatus) {
        if (!isAllowedTransition(roleId, orderItemId, targetStatus)) {
            return false;
        }

        int rows = jdbcTemplate.update(
                "UPDATE order_item SET item_status = ? WHERE order_item_id = ?",
                targetStatus, orderItemId
        );

        if (rows > 0) {
            syncOrderStatusByOrderItem(orderItemId);
            return true;
        }
        return false;
    }

    private void syncOrderStatusByOrderItem(int orderItemId) {
        Integer orderId = jdbcTemplate.queryForObject(
                "SELECT order_id FROM order_item WHERE order_item_id = ?",
                Integer.class,
                orderItemId
        );

        if (orderId == null) {
            return;
        }

        syncOrderStatus(orderId);
    }

    private void syncOrderStatus(int orderId) {
        Integer itemCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_item WHERE order_id = ?",
                Integer.class,
                orderId
        );

        if (itemCount == null || itemCount == 0) {
            jdbcTemplate.update("UPDATE `order` SET status = 'Pending', total_amount = 0 WHERE order_id = ?", orderId);
            return;
        }

        Integer unpaidCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_item WHERE order_id = ? AND item_status <> 'Paid'",
                Integer.class,
                orderId
        );

        if (unpaidCount != null && unpaidCount == 0) {
            jdbcTemplate.update("UPDATE `order` SET status = 'Checkout' WHERE order_id = ?", orderId);
        } else {
            jdbcTemplate.update("UPDATE `order` SET status = 'Pending' WHERE order_id = ?", orderId);
        }
    }

    private void recalculateOrderTotal(int orderId) {
        Double sum = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total), 0) FROM order_item WHERE order_id = ?",
                Double.class,
                orderId
        );
        jdbcTemplate.update("UPDATE `order` SET total_amount = ? WHERE order_id = ?", sum == null ? 0.0 : sum, orderId);
    }

    private boolean isAllowedTransition(int roleId, int orderItemId, String targetStatus) {
        String currentStatus = jdbcTemplate.queryForObject(
                "SELECT item_status FROM order_item WHERE order_item_id = ?",
                String.class,
                orderItemId
        );
        if (currentStatus == null) {
            return false;
        }

        String normalizedCurrent = normalizeStatus(currentStatus);
        String normalizedTarget = normalizeStatus(targetStatus);

        if (roleId == 3) { // chef
            return ("Pending".equals(normalizedCurrent) && "Accepted".equals(normalizedTarget))
                    || ("Accepted".equals(normalizedCurrent) && "Cooked".equals(normalizedTarget));
        }
        if (roleId == 2) { // waiter
            return "Cooked".equals(normalizedCurrent) && "Served".equals(normalizedTarget);
        }
        if (roleId == 4) { // cashier
            return "Served".equals(normalizedCurrent) && "Paid".equals(normalizedTarget);
        }
        return false;
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        String s = status.trim().toLowerCase();
        return switch (s) {
            case "waiting", "pending" -> "Pending";
            case "accepted", "cooking" -> "Accepted";
            case "cooked", "ready" -> "Cooked";
            case "served" -> "Served";
            case "paid", "checkout" -> "Paid";
            default -> status;
        };
    }
}
