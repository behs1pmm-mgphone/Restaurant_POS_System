package com.spring.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final JdbcTemplate jdbcTemplate;
    private final BillingService billingService;

    @Autowired
    public PaymentService(JdbcTemplate jdbcTemplate, BillingService billingService) {
        this.jdbcTemplate = jdbcTemplate;
        this.billingService = billingService;
        // Initialize database schema
        initializePaymentSchema();
    }
    
    private void initializePaymentSchema() {
        try {
            System.out.println("=== INITIALIZING PAYMENT TABLE SCHEMA ===");
            
            // Check if columns exist and add them if they don't
            String[] columnsToAdd = {
                "total_amount DECIMAL(10,2)",
                "subtotal DECIMAL(10,2)", 
                "tax DECIMAL(10,2)",
                "service_charge DECIMAL(10,2)",
                "grand_total DECIMAL(10,2)"
            };
            
            for (String columnDef : columnsToAdd) {
                String columnName = columnDef.split(" ")[0];
                try {
                    jdbcTemplate.queryForObject("SELECT " + columnName + " FROM payment LIMIT 1", Object.class);
                    System.out.println("Column " + columnName + " already exists");
                } catch (Exception e) {
                    // Column doesn't exist, add it
                    String alterSql = "ALTER TABLE payment ADD COLUMN " + columnDef;
                    jdbcTemplate.execute(alterSql);
                    System.out.println("Added column: " + columnName);
                }
            }
            
            System.out.println("=== PAYMENT TABLE SCHEMA INITIALIZED ===");
        } catch (Exception e) {
            System.err.println("Error initializing payment schema: " + e.getMessage());
        }
    }

    @Transactional
    public boolean createPayment(Integer orderId, String paymentMethod, BigDecimal totalAmount, Integer cashierId) {
        System.out.println("=== PAYMENT SERVICE DEBUG ===");
        System.out.println("Creating payment for Order ID: " + orderId);
        System.out.println("Payment Method: " + paymentMethod);
        System.out.println("Total Amount: " + totalAmount);
        System.out.println("Cashier ID: " + cashierId);
        
        try {
            // Update order billing with tax and service charge
            System.out.println("Updating order billing for Order ID: " + orderId);
            billingService.updateOrderWithBilling(orderId);
            
            // Get updated order details
            System.out.println("Fetching updated order details for Order ID: " + orderId);
            Map<String, Object> order = jdbcTemplate.queryForMap(
                "SELECT order_id, total_amount, tax, service_charge, o.status FROM `order` o WHERE o.order_id = ?",
                orderId
            );
            
            System.out.println("Order found: " + order);
            System.out.println("Order status: " + order.get("status"));
            System.out.println("Order total: " + order.get("total_amount"));

            if (!"Checkout".equals(order.get("status"))) {
                System.out.println("ERROR: Order status is not 'Checkout'. Current status: " + order.get("status"));
                throw new RuntimeException("Order must be in Checkout status to process payment");
            }
            
            // Use the calculated total amount from the order
            BigDecimal calculatedTotal = (BigDecimal) order.get("total_amount");
            System.out.println("=== AMOUNT DEBUG ===");
            System.out.println("Order total_amount from database: " + calculatedTotal);
            System.out.println("Order total_amount type: " + (calculatedTotal != null ? calculatedTotal.getClass().getName() : "null"));
            System.out.println("Order total_amount value: " + (calculatedTotal != null ? calculatedTotal.toPlainString() : "NULL"));
            
            // If total_amount is null or zero, try to recalculate from order items
            if (calculatedTotal == null || calculatedTotal.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("WARNING: total_amount is null or zero, attempting to recalculate...");
                try {
                    billingService.updateOrderWithBilling(orderId);
                    // Fetch updated order details
                    Map<String, Object> updatedOrder = jdbcTemplate.queryForMap(
                        "SELECT order_id, total_amount, tax, service_charge, o.status FROM `order` o WHERE o.order_id = ?",
                        orderId
                    );
                    calculatedTotal = (BigDecimal) updatedOrder.get("total_amount");
                    System.out.println("Recalculated total_amount: " + calculatedTotal);
                } catch (Exception recalcError) {
                    System.err.println("Failed to recalculate total: " + recalcError.getMessage());
                    throw new RuntimeException("Unable to determine order total amount");
                }
            }
            
            System.out.println("Final calculated total for payment: " + calculatedTotal);
            System.out.println("=== END AMOUNT DEBUG ===");

            // Check if payment already exists for this order
            System.out.println("Checking if payment already exists for Order ID: " + orderId);
            Integer existingPaymentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment WHERE order_id = ?",
                Integer.class,
                orderId
            );
            
            System.out.println("Existing payment count: " + existingPaymentCount);

            if (existingPaymentCount > 0) {
                System.out.println("ERROR: Payment already exists for this order");
                throw new RuntimeException("Payment already exists for this order");
            }

            // Create a simple sale_report record (required by payment table)
            System.out.println("Creating sale_report record...");
            Integer reportId;
            try {
                // First insert the record
                jdbcTemplate.update(
                    "INSERT INTO sale_report (report_date, total_sales, total_orders, created_by) VALUES (?, ?, ?, ?)",
                    LocalDateTime.now().toLocalDate(),
                    totalAmount,
                    1,
                    cashierId
                );
                
                // Then get the generated ID
                reportId = jdbcTemplate.queryForObject(
                    "SELECT LAST_INSERT_ID()",
                    Integer.class
                );
                System.out.println("Sale_report created with ID: " + reportId);
            } catch (Exception e) {
                // If sale_report table doesn't exist or fails, try to insert with a dummy report_id
                System.err.println("Warning: Could not create sale_report record: " + e.getMessage());
                // Try to use a default report_id (assuming there's at least one record)
                try {
                    reportId = jdbcTemplate.queryForObject(
                        "SELECT MIN(report_id) FROM sale_report LIMIT 1", 
                        Integer.class
                    );
                    System.out.println("Using existing sale_report ID: " + reportId);
                } catch (Exception ex) {
                    // If no sale_report records exist, create a minimal one
                    System.out.println("Creating sale_report table...");
                    jdbcTemplate.update(
                        "CREATE TABLE IF NOT EXISTS sale_report (report_id INT AUTO_INCREMENT PRIMARY KEY, report_date DATE, total_sales DECIMAL(10,2), total_orders INT, created_by INT)"
                    );
                    
                    // Insert the record
                    jdbcTemplate.update(
                        "INSERT INTO sale_report (report_date, total_sales, total_orders, created_by) VALUES (?, ?, ?, ?)",
                        LocalDateTime.now().toLocalDate(),
                        totalAmount,
                        1,
                        cashierId
                    );
                    
                    // Get the generated ID
                    reportId = jdbcTemplate.queryForObject(
                        "SELECT LAST_INSERT_ID()",
                        Integer.class
                    );
                    System.out.println("Sale_report table created and record inserted with ID: " + reportId);
                }
            }

            // Calculate proper invoice breakdown
            System.out.println("=== INVOICE CALCULATION DEBUG ===");
            BigDecimal subtotal = billingService.calculateSubtotal(orderId);
            BigDecimal tax = subtotal.multiply(billingService.getTaxRate()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal serviceCharge = subtotal.multiply(billingService.getServiceChargeRate()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal grandTotal = subtotal.add(tax).add(serviceCharge).setScale(2, RoundingMode.HALF_UP);
            
            System.out.println("Subtotal: " + subtotal);
            System.out.println("Tax (" + billingService.getTaxRate().multiply(new BigDecimal(100)) + "%): " + tax);
            System.out.println("Service Charge (" + billingService.getServiceChargeRate().multiply(new BigDecimal(100)) + "%): " + serviceCharge);
            System.out.println("Grand Total: " + grandTotal);
            System.out.println("=== END INVOICE CALCULATION ===");
            
            // Insert payment record with proper invoice breakdown
            System.out.println("Inserting payment record with invoice breakdown...");
            System.out.println("Payment details - Order ID: " + orderId + ", Method: " + paymentMethod + ", Report ID: " + reportId);
            
            int rowsAffected = jdbcTemplate.update(
                "INSERT INTO payment (order_id, final_amount, total_amount, subtotal, tax, service_charge, grand_total, payment_method, transaction_date, status, sale_report_report_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Success', ?)",
                orderId,
                grandTotal, // final_amount should be the grand total
                grandTotal, // total_amount should also be the grand total
                subtotal,
                tax,
                serviceCharge,
                grandTotal,
                paymentMethod,
                LocalDateTime.now(),
                reportId
            );
            
            System.out.println("Payment insertion result: " + rowsAffected + " rows affected");

            // Update order status to indicate payment completed
            System.out.println("Updating order status to 'Paid'...");
            int orderUpdateRows = jdbcTemplate.update(
                "UPDATE `order` o SET o.status = 'Paid' WHERE o.order_id = ?",
                orderId
            );
            
            System.out.println("Order status update result: " + orderUpdateRows + " rows affected");
            System.out.println("=== PAYMENT SERVICE COMPLETED SUCCESSFULLY ===");

            return rowsAffected > 0;

        } catch (Exception e) {
            System.err.println("Error creating payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getPaymentByOrderId(Integer orderId) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT p.payment_id, p.order_id, p.subtotal, p.tax, p.service_charge, p.grand_total, p.payment_method, p.transaction_date, p.status as payment_status, p.sale_report_report_id, p.created_at, " +
                "o.order_date, o.total_amount as order_total, o.status as order_status, t.table_number, u.user_name as cashier_name " +
                "FROM payment p " +
                "JOIN `order` o ON p.order_id = o.order_id " +
                "JOIN restaurant_table t ON o.restaurant_table_id = t.restaurant_table_id " +
                "JOIN `user` u ON o.created_by = u.user_id " +
                "WHERE p.order_id = ?",
                orderId
            );
        } catch (Exception e) {
            System.err.println("Error getting payment by order ID: " + e.getMessage());
            return null;
        }
    }

    public List<Map<String, Object>> getPaymentHistory(Integer cashierId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT p.*, o.order_date, t.table_number " +
                    "FROM payment p " +
                    "JOIN `order` o ON p.order_id = o.order_id " +
                    "JOIN restaurant_table t ON o.restaurant_table_id = t.restaurant_table_id " +
                    "WHERE o.created_by = ? AND p.transaction_date BETWEEN ? AND ? " +
                    "ORDER BY p.transaction_date DESC";
        
        return jdbcTemplate.queryForList(sql, cashierId, startDate, endDate);
    }

    public boolean validatePaymentAmount(Integer orderId, BigDecimal paymentAmount) {
        try {
            BigDecimal orderTotal = jdbcTemplate.queryForObject(
                "SELECT total_amount FROM `order` WHERE order_id = ?",
                BigDecimal.class,
                orderId
            );
            
            return orderTotal != null && orderTotal.compareTo(paymentAmount) == 0;
        } catch (Exception e) {
            System.err.println("Error validating payment amount: " + e.getMessage());
            return false;
        }
    }
}
