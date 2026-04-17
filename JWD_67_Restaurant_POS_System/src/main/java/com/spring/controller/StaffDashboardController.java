package com.spring.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.model.UserBean;
import com.spring.service.OrderWorkflowService;

import jakarta.servlet.http.HttpSession;

@Controller
public class StaffDashboardController {

    private final OrderWorkflowService orderWorkflowService;

    public StaffDashboardController(OrderWorkflowService orderWorkflowService) {
        this.orderWorkflowService = orderWorkflowService;
    }

    @GetMapping("/chef/dashboard")
    public String showChefDashboard(HttpSession session, Model model) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 3) {
            return "redirect:/login";
        }
        model.addAttribute("orderItems", orderWorkflowService.getOrderItemsBoard());
        return "chef-dashboard";
    }

    @GetMapping("/cashier/dashboard")
    public String showCashierDashboard(HttpSession session, Model model) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 4) {
            return "redirect:/login";
        }
        model.addAttribute("orderItems", orderWorkflowService.getOrderItemsBoard());
        return "cashier-dashboard";
    }

    @GetMapping("/chef/order-items")
    @ResponseBody
    public List<Map<String, Object>> chefBoard(HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 3) {
            return List.of();
        }
        return orderWorkflowService.getOrderItemsBoard();
    }

    @GetMapping("/cashier/order-items")
    @ResponseBody
    public List<Map<String, Object>> cashierBoard(HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 4) {
            return List.of();
        }
        return orderWorkflowService.getOrderItemsBoard();
    }

    @PostMapping("/chef/order-items/{id}/accept")
    @ResponseBody
    public Map<String, Object> acceptByChef(@PathVariable("id") Integer orderItemId, HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 3) {
            return Map.of("success", false, "message", "Unauthorized");
        }
        boolean success = orderWorkflowService.updateItemStatusByRole(3, orderItemId, "Accepted");
        return Map.of("success", success, "message", success ? "Item accepted." : "Cannot accept this item.");
    }

    @PostMapping("/chef/order-items/{id}/cook")
    @ResponseBody
    public Map<String, Object> cookByChef(@PathVariable("id") Integer orderItemId, HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 3) {
            return Map.of("success", false, "message", "Unauthorized");
        }
        boolean success = orderWorkflowService.updateItemStatusByRole(3, orderItemId, "Cooked");
        return Map.of("success", success, "message", success ? "Item marked as cooked." : "Cannot cook this item.");
    }

    @PostMapping("/cashier/order-items/{id}/pay")
    @ResponseBody
    public Map<String, Object> payByCashier(@PathVariable("id") Integer orderItemId, HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 4) {
            return Map.of("success", false, "message", "Unauthorized");
        }
        boolean success = orderWorkflowService.updateItemStatusByRole(4, orderItemId, "Paid");
        return Map.of("success", success, "message", success ? "Item paid." : "Only served items can be paid.");
    }
}
