package com.spring.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT payment_id, order_id, total_amount, final_amount, " +
                     "payment_method, transaction_date FROM payment " +
                     "WHERE DATE(transaction_date) BETWEEN ? AND ? " +
                     "AND status = 'Success' " +
                     "ORDER BY transaction_date DESC";

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public List<Map<String, Object>> findByDate(LocalDate date) {
        return findByDateRange(date, date);
    }

    public Map<String, Object> findByOrderId(Integer orderId) {
        try {
            String sql = "SELECT p.*, o.order_date, o.total_amount as order_total, t.table_number, u.user_name as cashier_name " +
                        "FROM payment p " +
                        "JOIN `order` o ON p.order_id = o.order_id " +
                        "JOIN restaurant_table t ON o.restaurant_table_id = t.restaurant_table_id " +
                        "JOIN `user` u ON o.created_by = u.user_id " +
                        "WHERE p.order_id = ?";
            
            return jdbcTemplate.queryForMap(sql, orderId);
        } catch (Exception e) {
            return null;
        }
    }

    public int savePayment(Integer orderId, java.math.BigDecimal totalAmount, java.math.BigDecimal finalAmount, 
                         String paymentMethod, LocalDateTime transactionDate, String status, Integer reportId) {
        String sql = "INSERT INTO payment (order_id, total_amount, final_amount, payment_method, transaction_date, status, sale_report_report_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        return jdbcTemplate.update(sql, orderId, totalAmount, finalAmount, paymentMethod, transactionDate, status, reportId);
    }

    public int updatePaymentStatus(Integer paymentId, String status) {
        String sql = "UPDATE payment SET status = ? WHERE payment_id = ?";
        return jdbcTemplate.update(sql, status, paymentId);
    }

    public boolean paymentExistsForOrder(Integer orderId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment WHERE order_id = ?", 
                Integer.class, 
                orderId
            );
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Map<String, Object>> findByCashierAndDateRange(Integer cashierId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT p.*, o.order_date, t.table_number " +
                    "FROM payment p " +
                    "JOIN `order` o ON p.order_id = o.order_id " +
                    "JOIN restaurant_table t ON o.restaurant_table_id = t.restaurant_table_id " +
                    "WHERE o.created_by = ? AND p.transaction_date BETWEEN ? AND ? " +
                    "ORDER BY p.transaction_date DESC";
        
        return jdbcTemplate.queryForList(sql, cashierId, startDate, endDate);
    }
}
