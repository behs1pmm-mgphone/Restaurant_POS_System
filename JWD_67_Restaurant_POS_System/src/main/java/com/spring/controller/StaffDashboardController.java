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
import org.springframework.jdbc.core.JdbcTemplate;

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
    private final JdbcTemplate jdbcTemplate;

    public StaffDashboardController(OrderWorkflowService orderWorkflowService, PdfService pdfService, PaymentService paymentService, BillingService billingService, JdbcTemplate jdbcTemplate) {
        this.orderWorkflowService = orderWorkflowService;
        this.pdfService = pdfService;
        this.paymentService = paymentService;
        this.billingService = billingService;
        this.jdbcTemplate = jdbcTemplate;
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

    @GetMapping("/test/pdf/{orderId}")
    public ResponseEntity<byte[]> testPdfGeneration(@PathVariable Integer orderId, HttpSession session) {
        System.out.println("=== TEST: PDF Generation Test for Order ID: " + orderId);
        
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 4) {
            System.out.println("ERROR: Unauthorized user for PDF test");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // Create test order data
            Map<String, Object> testOrder = new java.util.HashMap<>();
            testOrder.put("order_id", orderId);
            testOrder.put("table_number", "Test Table");
            testOrder.put("waiter_name", "Test Waiter");
            testOrder.put("order_date", new java.util.Date());
            testOrder.put("order_status", "Test");
            testOrder.put("total_amount", new java.math.BigDecimal("10000.00"));
            testOrder.put("tax", new java.math.BigDecimal("500.00"));
            testOrder.put("service_charge", new java.math.BigDecimal("200.00"));
            
            // Create test items
            List<Map<String, Object>> testItems = new java.util.ArrayList<>();
            Map<String, Object> item1 = new java.util.HashMap<>();
            item1.put("menu_name", "Test Item 1");
            item1.put("quantity", 2);
            item1.put("unit_price", 3000.00);
            item1.put("total", 6000.00);
            item1.put("note", "Test note");
            testItems.add(item1);
            
            Map<String, Object> item2 = new java.util.HashMap<>();
            item2.put("menu_name", "Test Item 2");
            item2.put("quantity", 1);
            item2.put("unit_price", 4000.00);
            item2.put("total", 4000.00);
            item2.put("note", "");
            testItems.add(item2);
            
            testOrder.put("items", testItems);
            
            // Create test payment data
            Map<String, Object> testPayment = new java.util.HashMap<>();
            testPayment.put("payment_id", 999);
            testPayment.put("order_id", orderId);
            testPayment.put("payment_method", "Cash");
            testPayment.put("transaction_date", new java.util.Date());
            testPayment.put("status", "Success");
            testPayment.put("subtotal", 10000.00);
            testPayment.put("tax", 500.00);
            testPayment.put("service_charge", 200.00);
            testPayment.put("grand_total", 10700.00);
            
            System.out.println("Generating test PDF...");
            
            // Generate PDF
            byte[] pdfContent = pdfService.generatePaymentReceipt(testOrder, testPayment);
            
            if (pdfContent == null || pdfContent.length == 0) {
                System.out.println("ERROR: Test PDF generation returned empty content");
                return ResponseEntity.internalServerError().build();
            }
            
            System.out.println("Test PDF generated successfully, size: " + pdfContent.length + " bytes");
            
            // Set headers for download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "test_order_" + orderId + "_receipt.pdf");
            headers.setContentLength(pdfContent.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
                    
        } catch (Exception e) {
            System.err.println("ERROR in test PDF generation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/public/test-pdf")
    public ResponseEntity<byte[]> publicTestPdf() {
        System.out.println("=== PUBLIC TEST: PDF Generation Test ===");
        
        try {
            // Create test order data
            Map<String, Object> testOrder = new java.util.HashMap<>();
            testOrder.put("order_id", 999);
            testOrder.put("table_number", "Test Table");
            testOrder.put("waiter_name", "Test Waiter");
            testOrder.put("order_date", new java.util.Date());
            testOrder.put("order_status", "Test");
            testOrder.put("total_amount", new java.math.BigDecimal("10000.00"));
            testOrder.put("tax", new java.math.BigDecimal("500.00"));
            testOrder.put("service_charge", new java.math.BigDecimal("200.00"));
            
            // Create test items
            List<Map<String, Object>> testItems = new java.util.ArrayList<>();
            Map<String, Object> item1 = new java.util.HashMap<>();
            item1.put("menu_name", "Test Item 1");
            item1.put("quantity", 2);
            item1.put("unit_price", 3000.00);
            item1.put("total", 6000.00);
            item1.put("note", "Test note");
            testItems.add(item1);
            
            Map<String, Object> item2 = new java.util.HashMap<>();
            item2.put("menu_name", "Test Item 2");
            item2.put("quantity", 1);
            item2.put("unit_price", 4000.00);
            item2.put("total", 4000.00);
            item2.put("note", "");
            testItems.add(item2);
            
            testOrder.put("items", testItems);
            
            // Create test payment data
            Map<String, Object> testPayment = new java.util.HashMap<>();
            testPayment.put("payment_id", 999);
            testPayment.put("order_id", 999);
            testPayment.put("payment_method", "Cash");
            testPayment.put("transaction_date", new java.util.Date());
            testPayment.put("status", "Success");
            testPayment.put("subtotal", 10000.00);
            testPayment.put("tax", 500.00);
            testPayment.put("service_charge", 200.00);
            testPayment.put("grand_total", 10700.00);
            
            System.out.println("Generating public test PDF...");
            
            // Generate PDF
            byte[] pdfContent = pdfService.generatePaymentReceipt(testOrder, testPayment);
            
            if (pdfContent == null || pdfContent.length == 0) {
                System.out.println("ERROR: Public test PDF generation returned empty content");
                return ResponseEntity.internalServerError().build();
            }
            
            System.out.println("Public test PDF generated successfully, size: " + pdfContent.length + " bytes");
            
            // Set headers for download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "public_test_receipt.pdf");
            headers.setContentLength(pdfContent.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
                    
        } catch (Exception e) {
            System.err.println("ERROR in public test PDF generation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/debug/payment-flow/{orderId}")
    @ResponseBody
    public Map<String, Object> debugPaymentFlow(@PathVariable Integer orderId, HttpSession session) {
        System.out.println("=== DEBUG: Complete Payment Flow Analysis for Order ID: " + orderId);
        
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 4) {
            return Map.of("success", false, "error", "Unauthorized user");
        }
        
        Map<String, Object> debugInfo = new java.util.HashMap<>();
        
        try {
            // 1. Check order exists
            Map<String, Object> order = jdbcTemplate.queryForMap(
                "SELECT order_id, status, total_amount, tax, service_charge, order_date FROM `order` WHERE order_id = ?",
                orderId
            );
            debugInfo.put("order", order);
            System.out.println("Order found: " + order);
            
            // 2. Check order items
            List<Map<String, Object>> items = jdbcTemplate.queryForList(
                "SELECT oi.*, mi.name AS menu_name FROM order_item oi JOIN menu_item mi ON oi.menu_item_id = mi.menu_item_id WHERE oi.order_id = ?",
                orderId
            );
            debugInfo.put("items", items);
            System.out.println("Items found: " + items.size());
            
            // 3. Check payment record
            Map<String, Object> payment = paymentService.getPaymentByOrderId(orderId);
            debugInfo.put("payment", payment);
            System.out.println("Payment found: " + (payment != null ? "YES" : "NO"));
            
            // 4. Test PDF generation with real data
            if (payment != null) {
                Map<String, Object> orderForPdf = new java.util.HashMap<>(order);
                orderForPdf.put("items", items);
                
                try {
                    byte[] pdfContent = pdfService.generatePaymentReceipt(orderForPdf, payment);
                    debugInfo.put("pdf_generated", true);
                    debugInfo.put("pdf_size", pdfContent.length);
                    System.out.println("PDF generation successful: " + pdfContent.length + " bytes");
                } catch (Exception pdfError) {
                    debugInfo.put("pdf_generated", false);
                    debugInfo.put("pdf_error", pdfError.getMessage());
                    System.err.println("PDF generation failed: " + pdfError.getMessage());
                }
            } else {
                debugInfo.put("pdf_generated", false);
                debugInfo.put("pdf_error", "No payment record found");
            }
            
            // 5. Check payment table structure
            try {
                List<Map<String, Object>> paymentColumns = jdbcTemplate.queryForList("DESCRIBE payment");
                debugInfo.put("payment_table_columns", paymentColumns);
            } catch (Exception e) {
                debugInfo.put("payment_table_error", e.getMessage());
            }
            
            debugInfo.put("success", true);
            return debugInfo;
            
        } catch (Exception e) {
            System.err.println("Error in payment flow debug: " + e.getMessage());
            e.printStackTrace();
            debugInfo.put("success", false);
            debugInfo.put("error", e.getMessage());
            return debugInfo;
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
            // First verify order exists and is paid
            Map<String, Object> orderCheck = jdbcTemplate.queryForMap(
                "SELECT order_id, status FROM `order` WHERE order_id = ?",
                orderId
            );
            
            if (orderCheck == null) {
                System.out.println("ERROR: Order not found");
                return ResponseEntity.notFound().build();
            }
            
            String orderStatus = (String) orderCheck.get("status");
            if (!"Paid".equals(orderStatus)) {
                System.out.println("ERROR: Order is not paid. Current status: " + orderStatus);
                return ResponseEntity.badRequest().build();
            }
            
            // Get order details directly from database for paid orders
            Map<String, Object> order = jdbcTemplate.queryForMap(
                "SELECT o.order_id, o.order_date, o.status AS order_status, o.total_amount, o.tax, o.service_charge, " +
                "t.table_number, u.user_name AS waiter_name " +
                "FROM `order` o " +
                "JOIN restaurant_table t ON o.restaurant_table_id = t.restaurant_table_id " +
                "JOIN `user` u ON o.created_by = u.user_id " +
                "WHERE o.order_id = ?",
                orderId
            );
            
            // Get order items for the receipt
            List<Map<String, Object>> items = jdbcTemplate.queryForList(
                "SELECT oi.order_item_id, oi.quantity, oi.unit_price, oi.total, oi.note, " +
                "mi.name AS menu_name " +
                "FROM order_item oi " +
                "JOIN menu_item mi ON oi.menu_item_id = mi.menu_item_id " +
                "WHERE oi.order_id = ? " +
                "ORDER BY oi.order_item_id",
                orderId
            );
            
            // Add items to order map for PDF generation
            order.put("items", items);
            
            // Get payment details for order
            Map<String, Object> payment = paymentService.getPaymentByOrderId(orderId);
            
            if (payment == null) {
                System.out.println("ERROR: Payment not found for order");
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("Generating PDF receipt for order: " + order.get("order_id"));
            System.out.println("Payment details: " + payment.get("payment_id"));
            
            // Generate PDF with payment details
            byte[] pdfContent = pdfService.generatePaymentReceipt(order, payment);
            
            if (pdfContent == null || pdfContent.length == 0) {
                System.out.println("ERROR: PDF generation returned empty content");
                return ResponseEntity.internalServerError().build();
            }
            
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
            
            // Return a more detailed error response
            try {
                String errorMsg = "PDF generation failed: " + e.getMessage();
                byte[] errorBytes = errorMsg.getBytes();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                return ResponseEntity.internalServerError()
                        .headers(headers)
                        .body(errorBytes);
            } catch (Exception ex) {
                return ResponseEntity.internalServerError().build();
            }
        }
    }

    @GetMapping("/cashier/orders/{id}/pdf")
    public ResponseEntity<byte[]> exportOrderPdf(@PathVariable("id") Integer orderId, HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        
        // Authorization Check
        if (loginUser == null || loginUser.getRoleId() == null || loginUser.getRoleId() != 4) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<Map<String, Object>> orders = orderWorkflowService.getOrdersBoard();
            
            Map<String, Object> order = orders.stream()
                    .filter(o -> {
                        Object id = o.get("order_id");
                        return id != null && id.equals(orderId);
                    })
                    .findFirst()
                    .orElse(null);
            
            if (order == null) return ResponseEntity.notFound().build();

            // --- ဒီအပိုင်းက Cashier နာမည်ကို Login User ဆီက ယူတာပါ ---
            order.put("cashier_name", loginUser.getUserName()); 

            Map<String, Object> payment = paymentService.getPaymentByOrderId(orderId);
            
            // DEBUG: Payment data တကယ်ရှိမရှိ console မှာ ကြည့်ဖို့
            System.out.println("DEBUG: Payment data for Order " + orderId + " => " + payment);

            byte[] pdfContent;
            // Payment data ရှိနေမှ generatePaymentReceipt ကို ခေါ်မှာပါ
            if (payment != null && !payment.isEmpty()) {
                pdfContent = pdfService.generatePaymentReceipt(order, payment);
            } else {
                pdfContent = pdfService.generateOrderReceipt(order);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "receipt_" + orderId + ".pdf");
            
            return ResponseEntity.ok().headers(headers).body(pdfContent);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}