package com.spring.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.repository.PaymentRepository;

@Service
public class SaleReportService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Map<String, Object> getReportSummaryByType(String type, LocalDate date) {
        LocalDate startDate, endDate, prevStartDate, prevEndDate;


        switch (type.toLowerCase()) {
            case "weekly":
                startDate = date.minusDays(date.getDayOfWeek().getValue() - 1);
                endDate = startDate.plusDays(6);
                prevStartDate = startDate.minusWeeks(1);
                prevEndDate = endDate.minusWeeks(1);
                break;
            case "monthly":
                startDate = date.with(TemporalAdjusters.firstDayOfMonth());
                endDate = date.with(TemporalAdjusters.lastDayOfMonth());
                prevStartDate = startDate.minusMonths(1);
                prevEndDate = prevStartDate.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case "yearly":
                startDate = date.with(TemporalAdjusters.firstDayOfYear());
                endDate = date.with(TemporalAdjusters.lastDayOfYear());
                prevStartDate = startDate.minusYears(1);
                prevEndDate = prevStartDate.with(TemporalAdjusters.lastDayOfYear());
                break;
            default: // daily
                startDate = date;
                endDate = date;
                prevStartDate = date.minusDays(1);
                prevEndDate = date.minusDays(1);
                break;
        }


        List<Map<String, Object>> currentPayments = paymentRepository.findByDateRange(startDate, endDate);
        BigDecimal currentRevenue = calculateTotal(currentPayments);


        List<Map<String, Object>> prevPayments = paymentRepository.findByDateRange(prevStartDate, prevEndDate);
        BigDecimal prevRevenue = calculateTotal(prevPayments);


        double growth = 0;
        if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = currentRevenue.subtract(prevRevenue)
                    .divide(prevRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100)).doubleValue();
        } else if (currentRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = 100;
        }


        Map<String, Object> data = new HashMap<>();
        data.put("payments", currentPayments);
        data.put("totalRevenue", currentRevenue);
        data.put("orderCount", currentPayments.size());
        data.put("growth", growth);


        BigDecimal avgValue = currentPayments.isEmpty() ? BigDecimal.ZERO :
                             currentRevenue.divide(new BigDecimal(currentPayments.size()), 2, RoundingMode.HALF_UP);
        data.put("avgOrderValue", avgValue);

        return data;
    }

    private BigDecimal calculateTotal(List<Map<String, Object>> payments) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> p : payments) {
            BigDecimal amount = (BigDecimal) p.get("final_amount");
            if (amount != null) total = total.add(amount);
        }
        return total;
    }
}