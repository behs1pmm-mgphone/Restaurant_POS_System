package com.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initializeDatabase() {
        System.out.println("=== DATABASE INITIALIZER: Initializing database tables ===");
        
        try {
            // Update order table status column to accommodate longer status values
            try {
                jdbcTemplate.execute("ALTER TABLE `order` MODIFY COLUMN status VARCHAR(50)");
                System.out.println("=== DATABASE INITIALIZER: Order status column updated ===");
            } catch (Exception e) {
                System.out.println("=== DATABASE INITIALIZER: Order status column already exists or update failed: " + e.getMessage() + " ===");
            }
            
            // Increase tax and service_charge column sizes to prevent truncation
            try {
                jdbcTemplate.execute("ALTER TABLE `order` MODIFY COLUMN tax DECIMAL(12,2)");
                jdbcTemplate.execute("ALTER TABLE `order` MODIFY COLUMN service_charge DECIMAL(12,2)");
                jdbcTemplate.execute("ALTER TABLE `order` MODIFY COLUMN total_amount DECIMAL(12,2)");
                System.out.println("=== DATABASE INITIALIZER: Order billing columns updated to DECIMAL(12,2) ===");
            } catch (Exception e) {
                System.out.println("=== DATABASE INITIALIZER: Order billing columns update failed: " + e.getMessage() + " ===");
            }
            
            // Create payment table if it doesn't exist with proper invoice fields
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS payment (" +
                "payment_id INT NOT NULL AUTO_INCREMENT, " +
                "order_id INT NOT NULL, " +
                "final_amount DECIMAL(10,2) NOT NULL, " +
                "payment_method VARCHAR(50) NOT NULL, " +
                "transaction_date DATETIME NOT NULL, " +
                "status VARCHAR(50) DEFAULT 'Success', " +
                "sale_report_report_id INT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (payment_id), " +
                "FOREIGN KEY (order_id) REFERENCES `order`(order_id), " +
                "FOREIGN KEY (sale_report_report_id) REFERENCES sale_report(report_id)" +
                ")"
            );
            
            // Add new invoice breakdown columns to existing payment table
            try {
                jdbcTemplate.execute("ALTER TABLE payment ADD COLUMN subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00");
                System.out.println("=== DATABASE INITIALIZER: Added subtotal column ===");
            } catch (Exception e) {
                System.out.println("=== DATABASE INITIALIZER: subtotal column already exists: " + e.getMessage() + " ===");
            }
            
            try {
                jdbcTemplate.execute("ALTER TABLE payment ADD COLUMN tax DECIMAL(10,2) NOT NULL DEFAULT 0.00");
                System.out.println("=== DATABASE INITIALIZER: Added tax column ===");
            } catch (Exception e) {
                System.out.println("=== DATABASE INITIALIZER: tax column already exists: " + e.getMessage() + " ===");
            }
            
            try {
                jdbcTemplate.execute("ALTER TABLE payment ADD COLUMN service_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00");
                System.out.println("=== DATABASE INITIALIZER: Added service_charge column ===");
            } catch (Exception e) {
                System.out.println("=== DATABASE INITIALIZER: service_charge column already exists: " + e.getMessage() + " ===");
            }
            
            try {
                jdbcTemplate.execute("ALTER TABLE payment ADD COLUMN grand_total DECIMAL(10,2) NOT NULL DEFAULT 0.00");
                System.out.println("=== DATABASE INITIALIZER: Added grand_total column ===");
            } catch (Exception e) {
                System.out.println("=== DATABASE INITIALIZER: grand_total column already exists: " + e.getMessage() + " ===");
            }
            
            System.out.println("=== DATABASE INITIALIZER: Payment table created/verified ===");
            
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
