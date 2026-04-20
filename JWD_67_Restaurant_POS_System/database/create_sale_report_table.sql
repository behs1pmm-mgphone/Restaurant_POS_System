-- Create sale_report table for payment processing
CREATE TABLE IF NOT EXISTS sale_report (
    report_id INT NOT NULL AUTO_INCREMENT,
    report_date DATE NOT NULL,
    total_sales DECIMAL(10,2) DEFAULT 0.00,
    total_orders INT DEFAULT 0,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (report_id)
);
