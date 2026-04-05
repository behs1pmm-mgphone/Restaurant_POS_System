package com.spring.repository;

import java.time.LocalDate;
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
}