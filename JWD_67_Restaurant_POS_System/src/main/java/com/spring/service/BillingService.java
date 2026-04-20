package com.spring.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class BillingService {

    private final JdbcTemplate jdbcTemplate;
    
    // Constants for tax and service charge
    private static final BigDecimal TAX_RATE = new BigDecimal("0.05"); // 5%
    private static final BigDecimal SERVICE_CHARGE_RATE = new BigDecimal("0.02"); // 2%

    @Autowired
    public BillingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Calculate tax and service charge for an order
     */
    public Map<String, BigDecimal> calculateOrderBilling(Integer orderId) {
        // Get the current order subtotal (sum of item totals)
        BigDecimal subtotal = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(total), 0) FROM order_item WHERE order_id = ?",
            BigDecimal.class,
            orderId
        );

        // Calculate tax (5% of subtotal)
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        
        // Calculate service charge (2% of subtotal)
        BigDecimal serviceCharge = subtotal.multiply(SERVICE_CHARGE_RATE).setScale(2, RoundingMode.HALF_UP);
        
        // Calculate grand total
        BigDecimal grandTotal = subtotal.add(tax).add(serviceCharge).setScale(2, RoundingMode.HALF_UP);

        return Map.of(
            "subtotal", subtotal,
            "tax", tax,
            "serviceCharge", serviceCharge,
            "grandTotal", grandTotal
        );
    }

    /**
     * Update order with tax and service charge calculations
     */
    public void updateOrderBilling(Integer orderId) {
        Map<String, BigDecimal> billing = calculateOrderBilling(orderId);
        
        jdbcTemplate.update(
            "UPDATE `order` SET " +
            "tax = ?, " +
            "service_charge = ?, " +
            "total_amount = ? " +
            "WHERE order_id = ?",
            billing.get("tax"),
            billing.get("serviceCharge"),
            billing.get("grandTotal"),
            orderId
        );
    }

    /**
     * Get tax rate
     */
    public BigDecimal getTaxRate() {
        return TAX_RATE;
    }

    /**
     * Get service charge rate
     */
    public BigDecimal getServiceChargeRate() {
        return SERVICE_CHARGE_RATE;
    }

    /**
     * Calculate subtotal from order items
     */
    public BigDecimal calculateSubtotal(Integer orderId) {
        return jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(total), 0) FROM order_item WHERE order_id = ?",
            BigDecimal.class,
            orderId
        );
    }

    /**
     * Update order with calculated tax, service charge, and total
     */
    public void updateOrderWithBilling(Integer orderId) {
        BigDecimal subtotal = calculateSubtotal(orderId);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal serviceCharge = subtotal.multiply(SERVICE_CHARGE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = subtotal.add(tax).add(serviceCharge).setScale(2, RoundingMode.HALF_UP);

        jdbcTemplate.update(
            "UPDATE `order` SET tax = ?, service_charge = ?, total_amount = ? WHERE order_id = ?",
            tax, serviceCharge, grandTotal, orderId
        );
    }
}
