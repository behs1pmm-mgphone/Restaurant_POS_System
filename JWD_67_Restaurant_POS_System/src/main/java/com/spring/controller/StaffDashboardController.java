package com.spring.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.model.UserBean;
import com.spring.service.BillingService;
import com.spring.service.OrderWorkflowService;
import com.spring.service.PdfService;
import com.spring.service.PaymentService;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
public class StaffDashboardController {

    private final OrderWorkflowService orderWorkflowService;
    private final PdfService pdfService;
    private final PaymentService paymentService;
    private final BillingService billingService;

    public StaffDashboardController(OrderWorkflowService orderWorkflowService, PdfService pdfService, PaymentService paymentService, BillingService billingService) {
        this.orderWorkflowService = orderWorkflowService;
        this.pdfService = pdfService;
        this.paymentService = paymentService;
        this.billingService = billingService;
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

    @GetMapping("/test/billing/{orderId}")
    @ResponseBody
    public Map<String, Object> testBilling(@PathVariable Integer orderId) {
        try {
            System.out.println("=== DEBUG: Manual billing test for order ID: " + orderId);
            billingService.updateOrderWithBilling(orderId);
            
            Map<String, java.math.BigDecimal> billingResult = billingService.calculateOrderBilling(orderId);
            System.out.println("=== DEBUG: Billing calculation result: " + billingResult);
            
            // Convert to Map<String, Object> for JSON response
            Map<String, Object> billing = new java.util.HashMap<>();
            billing.putAll(billingResult);
            
            return Map.of("success", true, "billing", billing);
        } catch (Exception e) {
            System.err.println("Error in billing test: " + e.getMessage());
            e.printStackTrace();
            return Map.of("success", false, "error", e.getMessage());
        }
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
    public Map<String, Object> checkoutOrder(@PathVariable("id") Integer orderId, 
                                           @RequestBody(required = false) Map<String, String> paymentData,
                                           HttpSession session) {
        System.out.println("=== DEBUG: Checkout Request ===");
        System.out.println("Order ID: " + orderId);
        
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 4) {
            System.out.println("ERROR: Unauthorized user");
            return Map.of("success", false, "message", "Unauthorized");
        }
        
        try {
            // First, checkout the order
            boolean checkoutSuccess = orderWorkflowService.checkoutOrderByCashier(orderId);
            if (!checkoutSuccess) {
                return Map.of(
                    "success", false,
                    "message", "Order cannot be checked out. Please ensure the order has items and is not already processed."
                );
            }
            
            // Get order details for payment processing and PDF generation
            List<Map<String, Object>> orders = orderWorkflowService.getOrdersBoard();
            Map<String, Object> order = orders.stream()
                    .filter(o -> {
                        Object orderIdObj = o.get("order_id");
                        return orderIdObj != null && orderIdObj.equals(orderId);
                    })
                    .findFirst()
                    .orElse(null);
            
            if (order == null) {
                return Map.of("success", false, "message", "Order not found");
            }
            
            java.math.BigDecimal totalAmount = (java.math.BigDecimal) order.get("total_amount");
            
            // Get payment method from request or default to Cash
            String paymentMethod = "Cash";
            if (paymentData != null && paymentData.containsKey("paymentMethod")) {
                paymentMethod = paymentData.get("paymentMethod");
            }
            
            // Create payment record - THIS IS THE CRITICAL STEP
            boolean paymentSuccess = paymentService.createPayment(
                orderId, 
                paymentMethod, 
                totalAmount, 
                loginUser.getUserId()
            );
            
            if (!paymentSuccess) {
                return Map.of(
                    "success", false, 
                    "message", "Payment processing failed."
                );
            }
            
            System.out.println("=== PAYMENT SUCCESSFUL ===");
            System.out.println("Order ID: " + orderId);
            System.out.println("Total Amount: " + totalAmount);
            System.out.println("Payment Method: " + paymentMethod);
            
            // Payment is successful - return success response
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "Payment Successful");
            response.put("orderId", orderId);
            response.put("totalAmount", totalAmount);
            response.put("paymentMethod", paymentMethod);
            
            return response;
                    
        } catch (Exception e) {
            System.err.println("Error during checkout: " + e.getMessage());
            e.printStackTrace();
            return Map.of("success", false, "message", "Checkout failed: " + e.getMessage());
        }
    }

    @GetMapping("/cashier/orders/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable("id") Integer orderId, HttpSession session) {
        System.out.println("=== DEBUG: Receipt Download Request ===");
        System.out.println("Order ID: " + orderId);
        
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 4) {
            System.out.println("ERROR: Unauthorized user");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // Get order details
            List<Map<String, Object>> orders = orderWorkflowService.getOrdersBoard();
            Map<String, Object> order = orders.stream()
                    .filter(o -> {
                        Object orderIdObj = o.get("order_id");
                        return orderIdObj != null && orderIdObj.equals(orderId);
                    })
                    .findFirst()
                    .orElse(null);
            
            if (order == null) {
                System.out.println("ERROR: Order not found");
                return ResponseEntity.notFound().build();
            }
            
            // Get payment details for order
            Map<String, Object> payment = paymentService.getPaymentByOrderId(orderId);
            
            if (payment == null) {
                System.out.println("ERROR: Payment not found for order");
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("Generating PDF receipt for order: " + order);
            
            // Generate PDF with payment details
            byte[] pdfContent = pdfService.generatePaymentReceipt(order, payment);
            System.out.println("PDF generated successfully, size: " + pdfContent.length + " bytes");
            
            // Set headers for automatic PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "order_" + orderId + "_receipt.pdf");
            headers.setContentLength(pdfContent.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
                    
        } catch (Exception e) {
            System.err.println("ERROR generating receipt PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
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
            
            // Get payment details for the order
            Map<String, Object> payment = paymentService.getPaymentByOrderId(orderId);
            
            // Generate PDF with payment details if payment exists, otherwise regular receipt
            byte[] pdfContent;
            if (payment != null) {
                pdfContent = pdfService.generatePaymentReceipt(order, payment);
            } else {
                pdfContent = pdfService.generateOrderReceipt(order);
            }
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
