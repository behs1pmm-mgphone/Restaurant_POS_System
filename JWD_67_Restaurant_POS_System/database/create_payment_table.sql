-- Create payment table for payment processing
CREATE TABLE IF NOT EXISTS payment (
    payment_id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    final_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    transaction_date DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'Success',
    sale_report_report_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (payment_id),
    FOREIGN KEY (order_id) REFERENCES `order`(order_id),
    FOREIGN KEY (sale_report_report_id) REFERENCES sale_report(report_id)
);
