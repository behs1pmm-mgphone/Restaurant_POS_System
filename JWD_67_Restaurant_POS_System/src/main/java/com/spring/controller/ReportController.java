package com.spring.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spring.service.SaleReportService;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private SaleReportService saleReportService;

    @GetMapping("/summary")
    public String getReportSummary(
            @RequestParam(name = "type", defaultValue = "daily") String type,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            jakarta.servlet.http.HttpSession session, // Session ကို parameter အနေနဲ့ ယူလိုက်ပါ
            Model model) {

        // --- LOGIN CHECK ---

        if (session.getAttribute("loginUser") == null) {

            return "redirect:/login";
        }
        // -------------------

        if (date == null) {
            date = LocalDate.now();
        }

        Map<String, Object> reportData = saleReportService.getReportSummaryByType(type, date);

        model.addAllAttributes(reportData);
        model.addAttribute("currentType", type);
        model.addAttribute("selectedDate", date);

        return "sale-report";
    }

    @GetMapping("/daily")
    public String dailyReportRedirect(
            @RequestParam(name = "date", required = false) LocalDate date,
            jakarta.servlet.http.HttpSession session) {


        if (session.getAttribute("loginUser") == null) {
            return "redirect:/login";
        }

        return "redirect:/admin/reports/summary?type=daily" + (date != null ? "&date=" + date : "");
    }
}