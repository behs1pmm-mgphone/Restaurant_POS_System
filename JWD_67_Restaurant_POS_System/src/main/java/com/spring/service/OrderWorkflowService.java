package com.spring.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spring.dto.OrderEditRequest;
import jakarta.servlet.http.HttpServletRequest;
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
                "WHERE o.status = 'Pending' " +
                "ORDER BY o.order_id DESC, oi.order_item_id ASC";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getOrdersBoard() {
        String sql =
                "SELECT o.order_id, o.order_date, o.status AS order_status, o.total_amount, " +
                "       t.table_number, u.user_name AS waiter_name, " +
                "       oi.order_item_id, oi.quantity, oi.unit_price, oi.total, oi.note, oi.item_status, " +
                "       mi.name AS menu_name " +
                "FROM `order` o " +
                "JOIN restaurant_table t ON o.restaurant_table_id = t.restaurant_table_id " +
                "JOIN `user` u ON o.created_by = u.user_id " +
                "LEFT JOIN order_item oi ON o.order_id = oi.order_id " +
                "LEFT JOIN menu_item mi ON oi.menu_item_id = mi.menu_item_id " +
                "ORDER BY o.order_id DESC, oi.order_item_id ASC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        Map<Integer, Map<String, Object>> orderMap = new HashMap<>();

        for (Map<String, Object> row : rows) {
            Integer orderId = (Integer) row.get("order_id");
            Map<String, Object> order = orderMap.computeIfAbsent(orderId, id -> {
                Map<String, Object> m = new HashMap<>();
                m.put("order_id", row.get("order_id"));
                m.put("order_date", row.get("order_date"));
                m.put("order_status", row.get("order_status"));
                m.put("table_number", row.get("table_number"));
                m.put("waiter_name", row.get("waiter_name"));
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
                item.put("menu_name", row.get("menu_name"));
                item.put("quantity", row.get("quantity"));
                item.put("unit_price", row.get("unit_price"));
                item.put("total", row.get("total"));
                item.put("note", row.get("note"));
                item.put("item_status", row.get("item_status"));
                items.add(item);
            }
        }

        for (Map<String, Object> order : orderMap.values()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
            boolean hasItems = !items.isEmpty();
            boolean allReserved = hasItems && items.stream().allMatch(i -> "Served".equals(i.get("item_status")));
            order.put("can_checkout", allReserved && "Pending".equals(order.get("order_status")));
        }

        return new ArrayList<>(orderMap.values());
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
        if ("Served".equals(itemStatus)) {
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

        Integer nonReservedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_item WHERE order_id = ? AND item_status <> 'Served'",
                Integer.class,
                orderId
        );

        if (nonReservedCount != null && nonReservedCount > 0) {
            jdbcTemplate.update("UPDATE `order` SET status = 'Pending' WHERE order_id = ?", orderId);
        }
    }

    public boolean checkoutOrderByCashier(int orderId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_item WHERE order_id = ?",
                Integer.class,
                orderId
        );
        if (count == null || count == 0) {
            return false;
        }

        Integer notReserved = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_item WHERE order_id = ? AND item_status <> 'Served'",
                Integer.class,
                orderId
        );
        if (notReserved != null && notReserved > 0) {
            return false;
        }

        int rows = jdbcTemplate.update(
                "UPDATE `order` SET status = 'Checkout' WHERE order_id = ? AND status = 'Pending'",
                orderId
        );
        return rows > 0;
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
        return false;
    }

    public Map<String, Object> getOrderDetailsForEdit(int orderId, int waiterId) {
        // Verify ownership and status
        Integer owns = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `order` WHERE order_id = ? AND created_by = ? AND status = 'Pending'",
                Integer.class,
                orderId, waiterId
        );
        if (owns == null || owns == 0) {
            throw new RuntimeException("Order not found or cannot be edited");
        }

        // Get order details
        Map<String, Object> order = jdbcTemplate.queryForMap(
                "SELECT o.order_id, o.order_date, o.total_amount, t.table_number " +
                "FROM `order` o " +
                "JOIN restaurant_table t ON o.restaurant_table_id = t.restaurant_table_id " +
                "WHERE o.order_id = ?",
                orderId
        );

        // Get order items
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
                "SELECT oi.order_item_id, oi.menu_item_id, oi.quantity, oi.unit_price, oi.total, oi.note, oi.item_status, " +
                "mi.name AS menu_name " +
                "FROM order_item oi " +
                "JOIN menu_item mi ON oi.menu_item_id = mi.menu_item_id " +
                "WHERE oi.order_id = ? AND oi.item_status NOT IN ('Served', 'Paid') " +
                "ORDER BY oi.order_item_id",
                orderId
        );

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        return result;
    }

    public boolean updateOrder(int orderId, int waiterId, HttpServletRequest request) {
        System.out.println("=== DEBUG: updateOrder called ===");
        System.out.println("Order ID: " + orderId);
        System.out.println("Waiter ID: " + waiterId);
        System.out.println("Request method: " + request.getMethod());
        System.out.println("Content type: " + request.getContentType());
        
        // Verify ownership
        try {
            Integer owns = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `order` WHERE order_id = ? AND created_by = ? AND status = 'Pending'",
                    Integer.class,
                    orderId, waiterId
            );
            System.out.println("Ownership check result: " + owns);
            if (owns == null || owns == 0) {
                throw new RuntimeException("Order not found or cannot be edited");
            }
        } catch (Exception e) {
            System.err.println("Error in ownership check: " + e.getMessage());
            throw new RuntimeException("Order not found or cannot be edited");
        }

        // Parse request parameters
        Map<String, String[]> paramMap = request.getParameterMap();
        System.out.println("Parameter map size: " + paramMap.size());
        System.out.println("Parameter keys: " + String.join(", ", paramMap.keySet()));
        
        List<Map<String, Object>> updatedItems = new ArrayList<>();
        
        // Find the maximum index to prepare the list
        int maxIndex = -1;
        for (String key : paramMap.keySet()) {
            System.out.println("Processing key: " + key);
            if (key.startsWith("items[") && key.contains("].menuItemId")) {
                try {
                    String indexStr = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
                    int index = Integer.parseInt(indexStr);
                    maxIndex = Math.max(maxIndex, index);
                    System.out.println("Found max index: " + maxIndex);
                } catch (Exception e) {
                    System.err.println("Error parsing index from key " + key + ": " + e.getMessage());
                }
            }
        }
        
        // Initialize the list with proper size
        for (int i = 0; i <= maxIndex; i++) {
            updatedItems.add(new HashMap<>());
        }
        System.out.println("Initialized items list with size: " + updatedItems.size());
        
        // Extract all item properties
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            System.out.println("Key: " + key + ", Values: " + java.util.Arrays.toString(values));
            
            if (key.startsWith("items[") && key.contains("].")) {
                try {
                    String indexStr = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
                    int index = Integer.parseInt(indexStr);
                    String property = key.substring(key.indexOf("].") + 2);
                    String value = values != null && values.length > 0 ? values[0] : "";
                    
                    System.out.println("Index: " + index + ", Property: " + property + ", Value: " + value);
                    
                    if (index < updatedItems.size()) {
                        Map<String, Object> item = updatedItems.get(index);
                        
                        switch (property) {
                            case "menuItemId" -> {
                                if (!value.isEmpty()) {
                                    item.put("menuItemId", Integer.parseInt(value));
                                    System.out.println("Set menuItemId: " + value);
                                }
                            }
                            case "quantity" -> {
                                if (!value.isEmpty()) {
                                    item.put("quantity", Integer.parseInt(value));
                                    System.out.println("Set quantity: " + value);
                                }
                            }
                            case "note" -> {
                                item.put("note", value);
                                System.out.println("Set note: " + value);
                            }
                            case "unitPrice" -> {
                                if (!value.isEmpty()) {
                                    item.put("unitPrice", Double.parseDouble(value));
                                    System.out.println("Set unitPrice: " + value);
                                }
                            }
                            case "total" -> {
                                if (!value.isEmpty()) {
                                    item.put("total", Double.parseDouble(value));
                                    System.out.println("Set total: " + value);
                                }
                            }
                            case "orderItemId" -> {
                                if (!value.isEmpty()) {
                                    item.put("orderItemId", Integer.parseInt(value));
                                    System.out.println("Set orderItemId: " + value);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing entry " + key + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("Final items list size: " + updatedItems.size());
        for (int i = 0; i < updatedItems.size(); i++) {
            System.out.println("Item " + i + ": " + updatedItems.get(i));
        }
        
        // Remove empty items (items without menuItemId)
        updatedItems.removeIf(item -> !item.containsKey("menuItemId") || item.get("menuItemId") == null);
        
        System.out.println("After removing empty items: " + updatedItems.size());
        
        if (updatedItems.isEmpty()) {
            throw new RuntimeException("Order must have at least one item");
        }

        // Get existing items to determine what to delete/update/insert
        List<Integer> existingItemIds = jdbcTemplate.queryForList(
                "SELECT order_item_id FROM order_item WHERE order_id = ?",
                Integer.class,
                orderId
        );
        
        List<Integer> updatedItemIds = new ArrayList<>();
        for (Map<String, Object> item : updatedItems) {
            if (item.containsKey("orderItemId")) {
                updatedItemIds.add((Integer) item.get("orderItemId"));
            }
        }
        
        // Delete items that are no longer in the updated list
        for (Integer existingId : existingItemIds) {
            if (!updatedItemIds.contains(existingId)) {
                jdbcTemplate.update("DELETE FROM order_item WHERE order_item_id = ?", existingId);
            }
        }
        
        // Update or insert items
        for (Map<String, Object> item : updatedItems) {
            Integer menuItemId = (Integer) item.get("menuItemId");
            Integer quantity = (Integer) item.get("quantity");
            String note = (String) item.get("note");
            Double unitPrice = (Double) item.get("unitPrice");
            Double total = (Double) item.get("total");
            
            if (item.containsKey("orderItemId")) {
                // Update existing item
                Integer orderItemId = (Integer) item.get("orderItemId");
                jdbcTemplate.update(
                        "UPDATE order_item SET menu_item_id = ?, quantity = ?, note = ?, unit_price = ?, total = ? WHERE order_item_id = ?",
                        menuItemId, quantity, note, unitPrice, total, orderItemId
                );
            } else {
                // Insert new item
                jdbcTemplate.update(
                        "INSERT INTO order_item (order_id, menu_item_id, note, quantity, unit_price, total, item_status) VALUES (?, ?, ?, ?, ?, ?, 'Pending')",
                        orderId, menuItemId, note, quantity, unitPrice, total
                );
            }
        }
        
        // Recalculate order total
        recalculateOrderTotal(orderId);
        return true;
    }

    public boolean updateOrderWithJson(int orderId, int waiterId, OrderEditRequest editRequest) {
        System.out.println("=== DEBUG: updateOrderWithJson called ===");
        System.out.println("Order ID: " + orderId);
        System.out.println("Waiter ID: " + waiterId);
        
        // Verify ownership
        try {
            Integer owns = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `order` WHERE order_id = ? AND created_by = ? AND status = 'Pending'",
                    Integer.class,
                    orderId, waiterId
            );
            System.out.println("Ownership check result: " + owns);
            if (owns == null || owns == 0) {
                throw new RuntimeException("Order not found or cannot be edited");
            }
        } catch (Exception e) {
            System.err.println("Error in ownership check: " + e.getMessage());
            throw new RuntimeException("Order not found or cannot be edited");
        }

        List<OrderEditRequest.OrderItemEdit> updatedItems = editRequest.getItems();
        System.out.println("Items to update: " + updatedItems.size());
        
        for (int i = 0; i < updatedItems.size(); i++) {
            OrderEditRequest.OrderItemEdit item = updatedItems.get(i);
            System.out.println("Item " + i + ": " + item.getMenuItemId() + " x" + item.getQuantity() + " (" + item.getNote() + ")");
        }
        
        if (updatedItems == null || updatedItems.isEmpty()) {
            throw new RuntimeException("Order must have at least one item");
        }

        // Get existing items to determine what to delete/update/insert
        List<Integer> existingItemIds = jdbcTemplate.queryForList(
                "SELECT order_item_id FROM order_item WHERE order_id = ?",
                Integer.class,
                orderId
        );
        
        List<Integer> updatedItemIds = new ArrayList<>();
        for (OrderEditRequest.OrderItemEdit item : updatedItems) {
            if (item.getOrderItemId() != null) {
                updatedItemIds.add(item.getOrderItemId());
            }
        }
        
        // Delete items that are no longer in the updated list
        for (Integer existingId : existingItemIds) {
            if (!updatedItemIds.contains(existingId)) {
                jdbcTemplate.update("DELETE FROM order_item WHERE order_item_id = ?", existingId);
                System.out.println("Deleted item: " + existingId);
            }
        }
        
        // Update or insert items
        for (OrderEditRequest.OrderItemEdit item : updatedItems) {
            Integer menuItemId = item.getMenuItemId();
            Integer quantity = item.getQuantity();
            String note = item.getNote();
            Double unitPrice = item.getUnitPrice();
            Double total = item.getTotal();
            
            if (item.getOrderItemId() != null) {
                // Update existing item
                Integer orderItemId = item.getOrderItemId();
                jdbcTemplate.update(
                        "UPDATE order_item SET menu_item_id = ?, quantity = ?, note = ?, unit_price = ?, total = ? WHERE order_item_id = ?",
                        menuItemId, quantity, note, unitPrice, total, orderItemId
                );
                System.out.println("Updated item: " + orderItemId);
            } else {
                // Insert new item
                jdbcTemplate.update(
                        "INSERT INTO order_item (order_id, menu_item_id, note, quantity, unit_price, total, item_status) VALUES (?, ?, ?, ?, ?, ?, 'Pending')",
                        orderId, menuItemId, note, quantity, unitPrice, total
                );
                System.out.println("Inserted new item: " + menuItemId);
            }
        }
        
        // Recalculate order total
        recalculateOrderTotal(orderId);
        return true;
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
            case "served", "reserved", "paid", "checkout" -> "Served";
            default -> status;
        };
    }
}
