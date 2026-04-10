package com.spring.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.spring.model.OrderItem;

@Repository
public class OrderItemRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(OrderItem item) {

        String sql = "INSERT INTO `order_item` (order_id, menu_item_id, note, quantity, unit_price, total, item_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'Pending')";

        jdbcTemplate.update(sql,
            item.getOrderId(),
            item.getMenuItemId(),
            item.getNote(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getTotal()
        );
    }
}