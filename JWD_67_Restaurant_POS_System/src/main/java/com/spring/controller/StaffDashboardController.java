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
import com.spring.service.PdfService;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
public class StaffDashboardController {

    private final OrderWorkflowService orderWorkflowService;
    private final PdfService pdfService;

    public StaffDashboardController(OrderWorkflowService orderWorkflowService, PdfService pdfService) {
        this.orderWorkflowService = orderWorkflowService;
        this.pdfService = pdfService;
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

    @GetMapping("/cashier/orders")
    @ResponseBody
    public List<Map<String, Object>> cashierOrders(HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 4) {
            return List.of();
        }
        return orderWorkflowService.getOrdersBoard();
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
        return Map.of("success", false, "message", "Item-level payment is disabled. Use order checkout.");
    }

    @PostMapping("/cashier/orders/{id}/checkout")
    @ResponseBody
    public Map<String, Object> checkoutOrder(@PathVariable("id") Integer orderId, HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 4) {
            return Map.of("success", false, "message", "Unauthorized");
        }
        boolean success = orderWorkflowService.checkoutOrderByCashier(orderId);
        return Map.of(
                "success", success,
                "message", success
                        ? "Order moved to Checkout."
                        : "Order can be checked out only when all items are Reserved."
        );
    }

    @GetMapping("/cashier/orders/{id}/pdf")
    public ResponseEntity<byte[]> exportOrderPdf(@PathVariable("id") Integer orderId, HttpSession session) {
        System.out.println("=== DEBUG: PDF Export Request ===");
        System.out.println("Order ID: " + orderId);
        
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 4) {
            System.out.println("ERROR: Unauthorized user");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // Get order details
            List<Map<String, Object>> orders = orderWorkflowService.getOrdersBoard();
            System.out.println("Total orders found: " + orders.size());
            
            Map<String, Object> order = orders.stream()
                    .filter(o -> {
                        Object orderIdObj = o.get("order_id");
                        boolean matches = orderIdObj != null && orderIdObj.equals(orderId);
                        if (matches) {
                            System.out.println("Found order: " + o);
                        }
                        return matches;
                    })
                    .findFirst()
                    .orElse(null);
            
            if (order == null) {
                System.out.println("ERROR: Order not found");
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Generating PDF for order: " + order);
            
            // Generate PDF
            byte[] pdfContent = pdfService.generateOrderReceipt(order);
            System.out.println("PDF generated successfully, size: " + pdfContent.length + " bytes");
            
            // Set headers for download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "order_" + orderId + "_receipt.pdf");
            headers.setContentLength(pdfContent.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
                    
        } catch (Exception e) {
            System.err.println("ERROR generating PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
