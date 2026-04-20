package com.spring.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.element.LineSeparator;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    /**
     * Enhanced PDF generation with comprehensive debugging and error handling
     */
    public byte[] generatePaymentReceipt(Map<String, Object> order, Map<String, Object> payment) throws IOException {
        System.out.println("=== DEBUG: Enhanced Payment PDF Generation Started ===");
        
        // Debug input data
        System.out.println("Order data: " + order);
        System.out.println("Payment data: " + payment);
        
        // Validate input data
        if (order == null) {
            throw new IOException("Order data is null");
        }
        if (payment == null) {
            throw new IOException("Payment data is null");
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            // Create PDF document with enhanced error handling
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            System.out.println("PDF document created successfully");
            
            // Add restaurant header
            document.add(new Paragraph("RESTAURANT POS SYSTEM")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold());
            
            document.add(new Paragraph("PAYMENT RECEIPT")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setMarginBottom(20));
            
            // Add order details
            document.add(new Paragraph("Order Details")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            // Order ID
            String orderId = safeToString(order.get("order_id"), "N/A");
            document.add(new Paragraph("Order #: " + orderId).setFontSize(12));
            
            // Table number
            String tableNumber = safeToString(order.get("table_number"), "N/A");
            document.add(new Paragraph("Table: " + tableNumber).setFontSize(12));
            
            // Waiter name
            String waiterName = safeToString(order.get("waiter_name"), "N/A");
            document.add(new Paragraph("Waiter: " + waiterName).setFontSize(12));
            
            // Order date
            String orderDate = formatDate(order.get("order_date"), dateFormat);
            document.add(new Paragraph("Date: " + orderDate).setFontSize(12));
            
            // Order status
            String orderStatus = safeToString(order.get("order_status"), "N/A");
            document.add(new Paragraph("Status: " + orderStatus).setFontSize(12));
            
            document.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(10)
                    .setMarginBottom(10));
            
            // Add items table
            document.add(new Paragraph("Order Items")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));
            
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2}))
                    .useAllAvailableWidth();
            
            // Table headers
            table.addHeaderCell(new Cell().add(new Paragraph("Item Name").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold().setTextAlignment(TextAlignment.CENTER)));
            table.addHeaderCell(new Cell().add(new Paragraph("Price").setBold().setTextAlignment(TextAlignment.RIGHT)));
            table.addHeaderCell(new Cell().add(new Paragraph("Total").setBold().setTextAlignment(TextAlignment.RIGHT)));
            
            // Add items
            List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
            System.out.println("Processing items: " + (items != null ? items.size() : 0) + " items found");
            
            if (items != null && !items.isEmpty()) {
                for (Map<String, Object> item : items) {
                    try {
                        String itemName = safeToString(item.get("menu_name"), "Unknown Item");
                        Integer quantity = safeToInteger(item.get("quantity"), 0);
                        Double unitPrice = safeToDouble(item.get("unit_price"), 0.0);
                        Double total = safeToDouble(item.get("total"), 0.0);
                        String note = safeToString(item.get("note"), "");
                        
                        // Item row
                        table.addCell(new Cell().add(new Paragraph(itemName)));
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(quantity)).setTextAlignment(TextAlignment.CENTER)));
                        table.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", unitPrice)).setTextAlignment(TextAlignment.RIGHT)));
                        table.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", total)).setTextAlignment(TextAlignment.RIGHT)));
                        
                        // Note row if exists
                        if (note != null && !note.trim().isEmpty()) {
                            table.addCell(new Cell(1, 4).add(new Paragraph("Note: " + note)
                                    .setFontSize(10)
                                    .setItalic()
                                    .setTextAlignment(TextAlignment.LEFT)));
                        }
                        
                        System.out.println("Added item: " + itemName + " x" + quantity);
                    } catch (Exception e) {
                        System.err.println("Error processing item: " + e.getMessage());
                        // Add a placeholder row for the failed item
                        table.addCell(new Cell().add(new Paragraph("Error loading item")));
                        table.addCell(new Cell().add(new Paragraph("-").setTextAlignment(TextAlignment.CENTER)));
                        table.addCell(new Cell().add(new Paragraph("-").setTextAlignment(TextAlignment.RIGHT)));
                        table.addCell(new Cell().add(new Paragraph("-").setTextAlignment(TextAlignment.RIGHT)));
                    }
                }
            } else {
                // Add a "No items" row
                table.addCell(new Cell(1, 4).add(new Paragraph("No items found").setTextAlignment(TextAlignment.CENTER)));
            }
            
            document.add(table);
            
            document.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(10)
                    .setMarginBottom(10));
            
            // Add payment details
            document.add(new Paragraph("Payment Details")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));
            
            String paymentMethod = safeToString(payment.get("payment_method"), "N/A");
            document.add(new Paragraph("Payment Method: " + paymentMethod).setFontSize(12));
            
            String transactionDate = formatDate(payment.get("transaction_date"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            document.add(new Paragraph("Transaction Date: " + transactionDate).setFontSize(12));
            
            String paymentStatus = safeToString(payment.get("status"), "N/A");
            document.add(new Paragraph("Payment Status: " + paymentStatus).setFontSize(12));
            
            document.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(10)
                    .setMarginBottom(10));
            
            // Add billing summary
            document.add(new Paragraph("Billing Summary")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));
            
            // Get amounts from payment data first, then fallback to order data
            Double subtotal = safeToDouble(payment.get("subtotal"), safeToDouble(order.get("subtotal"), 0.0));
            Double tax = safeToDouble(payment.get("tax"), safeToDouble(order.get("tax"), 0.0));
            Double serviceCharge = safeToDouble(payment.get("service_charge"), safeToDouble(order.get("service_charge"), 0.0));
            Double totalAmount = safeToDouble(payment.get("grand_total"), safeToDouble(payment.get("final_amount"), safeToDouble(order.get("total_amount"), 0.0)));
            
            System.out.println("Billing amounts - Subtotal: " + subtotal + ", Tax: " + tax + ", Service: " + serviceCharge + ", Total: " + totalAmount);
            
            // Create billing table
            Table billingTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .useAllAvailableWidth()
                    .setMarginBottom(10);
            
            billingTable.addHeaderCell(new Cell().add(new Paragraph("Item").setBold()));
            billingTable.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold().setTextAlignment(TextAlignment.RIGHT)));
            
            billingTable.addCell(new Cell().add(new Paragraph("Subtotal")));
            billingTable.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", subtotal)).setTextAlignment(TextAlignment.RIGHT)));
            
            billingTable.addCell(new Cell().add(new Paragraph("Tax (5%)")));
            billingTable.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", tax)).setTextAlignment(TextAlignment.RIGHT)));
            
            billingTable.addCell(new Cell().add(new Paragraph("Service Charge (2%)")));
            billingTable.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", serviceCharge)).setTextAlignment(TextAlignment.RIGHT)));
            
            document.add(billingTable);
            
            // Add total
            document.add(new Paragraph("TOTAL PAID: " + String.format("%.0f MMK", totalAmount))
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(10));
            
            // Add footer
            document.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(20));
            
            document.add(new Paragraph("Thank you for your order!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setMarginTop(10));
            
            document.add(new Paragraph("Generated on: " + dateFormat.format(new Date()))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setMarginTop(5));
            
            // Close document properly
            document.close();
            
            byte[] result = baos.toByteArray();
            System.out.println("Payment PDF generated successfully, size: " + result.length + " bytes");
            
            // Validate PDF content
            if (result.length == 0) {
                throw new IOException("Generated PDF is empty");
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("ERROR in payment PDF generation: " + e.getMessage());
            e.printStackTrace();
            
            // Try to generate a simple fallback PDF
            return generateFallbackPDF(order, payment);
        }
    }
    
    public byte[] generateOrderReceipt(Map<String, Object> order) throws IOException {
        System.out.println("=== DEBUG: Order PDF Generation Started ===");
        System.out.println("Order data: " + order);
        
        // Validate input data
        if (order == null) {
            throw new IOException("Order data is null");
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            System.out.println("PDF document created successfully");
        
        // Add restaurant header
        document.add(new Paragraph("RESTAURANT POS SYSTEM")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold());
        
        document.add(new Paragraph("Order Receipt")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16)
                .setMarginBottom(20));
        
        // Add order details
        document.add(new Paragraph("Order Details")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String orderDate;
        Object orderDateObj = order.get("order_date");
        if (orderDateObj != null) {
            if (orderDateObj instanceof String) {
                // If it's already a string, use it directly or parse it
                orderDate = (String) orderDateObj;
            } else if (orderDateObj instanceof java.util.Date) {
                // If it's a Date object, format it
                orderDate = dateFormat.format((java.util.Date) orderDateObj);
            } else {
                // Try to convert to string
                orderDate = String.valueOf(orderDateObj);
            }
        } else {
            orderDate = dateFormat.format(new Date());
        }
        
        document.add(new Paragraph("Order #: " + order.get("order_id"))
                .setFontSize(12));
        document.add(new Paragraph("Table: " + order.get("table_number"))
                .setFontSize(12));
        document.add(new Paragraph("Waiter: " + (order.get("waiter_name") != null ? order.get("waiter_name") : "N/A"))
                .setFontSize(12));
        document.add(new Paragraph("Date: " + orderDate)
                .setFontSize(12));
        document.add(new Paragraph("Status: " + order.get("order_status"))
                .setFontSize(12));
        
        document.add(new LineSeparator(new SolidLine(1f))
                .setMarginTop(10)
                .setMarginBottom(10));
        
        // Add items table
        document.add(new Paragraph("Order Items")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2}))
                .useAllAvailableWidth();
        
        // Table headers
        table.addHeaderCell(new Cell().add(new Paragraph("Item Name").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold().setTextAlignment(TextAlignment.CENTER)));
        table.addHeaderCell(new Cell().add(new Paragraph("Price").setBold().setTextAlignment(TextAlignment.RIGHT)));
        table.addHeaderCell(new Cell().add(new Paragraph("Total").setBold().setTextAlignment(TextAlignment.RIGHT)));
        
        // Add items
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
        if (items != null) {
            for (Map<String, Object> item : items) {
                String itemName = (String) item.get("menu_name");
                Integer quantity = (Integer) item.get("quantity");
                
                // Handle BigDecimal to Double conversion
                Object unitPriceObj = item.get("unit_price");
                Object totalObj = item.get("total");
                Double unitPrice = unitPriceObj != null ? 
                    ((java.math.BigDecimal) unitPriceObj).doubleValue() : 0.0;
                Double total = totalObj != null ? 
                    ((java.math.BigDecimal) totalObj).doubleValue() : 0.0;
                
                String note = (String) item.get("note");
                
                // Item row
                table.addCell(new Cell().add(new Paragraph(itemName)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(quantity)).setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", unitPrice)).setTextAlignment(TextAlignment.RIGHT)));
                table.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", total)).setTextAlignment(TextAlignment.RIGHT)));
                
                // Note row if exists
                if (note != null && !note.trim().isEmpty()) {
                    table.addCell(new Cell(1, 4).add(new Paragraph("Note: " + note)
                            .setFontSize(10)
                            .setItalic()
                            .setTextAlignment(TextAlignment.LEFT)));
                }
            }
        }
        
        document.add(table);
        
        document.add(new LineSeparator(new SolidLine(1f))
                .setMarginTop(10)
                .setMarginBottom(10));
        
        // Add total
        Object totalAmountObj = order.get("total_amount");
        Double totalAmount = totalAmountObj != null ? 
            ((java.math.BigDecimal) totalAmountObj).doubleValue() : 0.0;
        document.add(new Paragraph("TOTAL: " + String.format("%.0f MMK", totalAmount))
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10));
        
        // Add footer
        document.add(new LineSeparator(new SolidLine(1f))
                .setMarginTop(20));
        
        document.add(new Paragraph("Thank you for your order!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12)
                .setMarginTop(10));
        
        document.add(new Paragraph("Generated on: " + dateFormat.format(new Date()))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setMarginTop(5));
        
            document.close();
            
            byte[] result = baos.toByteArray();
            System.out.println("PDF completed successfully, size: " + result.length + " bytes");
            return result;
            
        } catch (Exception e) {
            System.err.println("ERROR in PDF generation: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    
    // Helper methods for enhanced PDF generation
    private void debugMapData(Map<String, Object> data, String dataType) {
        if (data == null) {
            System.out.println(dataType + " data: NULL");
            return;
        }
        
        System.out.println(dataType + " data keys: " + String.join(", ", data.keySet()));
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            String valueStr = value != null ? 
                (value instanceof List ? "List[" + ((List<?>) value).size() + "]" : value.toString()) : 
                "NULL";
            System.out.println("  " + entry.getKey() + ": " + valueStr + " (" + 
                (value != null ? value.getClass().getSimpleName() : "null") + ")");
        }
    }
    
    private void addRestaurantHeader(Document document) {
        try {
            document.add(new Paragraph("RESTAURANT POS SYSTEM")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold());
            
            document.add(new Paragraph("PAYMENT RECEIPT")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setMarginBottom(20));
                    
            System.out.println("Restaurant header added successfully");
        } catch (Exception e) {
            System.err.println("Error adding restaurant header: " + e.getMessage());
        }
    }
    
    private void addOrderDetails(Document document, Map<String, Object> order) {
        try {
            document.add(new Paragraph("Order Details")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            // Order ID
            String orderId = safeToString(order.get("order_id"), "N/A");
            document.add(new Paragraph("Order #: " + orderId).setFontSize(12));
            
            // Table number
            String tableNumber = safeToString(order.get("table_number"), "N/A");
            document.add(new Paragraph("Table: " + tableNumber).setFontSize(12));
            
            // Waiter name
            String waiterName = safeToString(order.get("waiter_name"), "N/A");
            document.add(new Paragraph("Waiter: " + waiterName).setFontSize(12));
            
            // Order date
            String orderDate = formatDate(order.get("order_date"), dateFormat);
            document.add(new Paragraph("Date: " + orderDate).setFontSize(12));
            
            // Order status
            String orderStatus = safeToString(order.get("order_status"), "N/A");
            document.add(new Paragraph("Status: " + orderStatus).setFontSize(12));
            
            document.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(10)
                    .setMarginBottom(10));
                    
            System.out.println("Order details added successfully");
        } catch (Exception e) {
            System.err.println("Error adding order details: " + e.getMessage());
        }
    }
    
    private void addItemsTable(Document document, Map<String, Object> order) {
        try {
            document.add(new Paragraph("Order Items")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));
            
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2}))
                    .useAllAvailableWidth();
            
            // Table headers
            table.addHeaderCell(new Cell().add(new Paragraph("Item Name").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold().setTextAlignment(TextAlignment.CENTER)));
            table.addHeaderCell(new Cell().add(new Paragraph("Price").setBold().setTextAlignment(TextAlignment.RIGHT)));
            table.addHeaderCell(new Cell().add(new Paragraph("Total").setBold().setTextAlignment(TextAlignment.RIGHT)));
            
            // Add items
            List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
            System.out.println("Processing items: " + (items != null ? items.size() : 0) + " items found");
            
            if (items != null && !items.isEmpty()) {
                for (Map<String, Object> item : items) {
                    try {
                        String itemName = safeToString(item.get("menu_name"), "Unknown Item");
                        Integer quantity = safeToInteger(item.get("quantity"), 0);
                        Double unitPrice = safeToDouble(item.get("unit_price"), 0.0);
                        Double total = safeToDouble(item.get("total"), 0.0);
                        String note = safeToString(item.get("note"), "");
                        
                        // Item row
                        table.addCell(new Cell().add(new Paragraph(itemName)));
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(quantity)).setTextAlignment(TextAlignment.CENTER)));
                        table.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", unitPrice)).setTextAlignment(TextAlignment.RIGHT)));
                        table.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", total)).setTextAlignment(TextAlignment.RIGHT)));
                        
                        // Note row if exists
                        if (note != null && !note.trim().isEmpty()) {
                            table.addCell(new Cell(1, 4).add(new Paragraph("Note: " + note)
                                    .setFontSize(10)
                                    .setItalic()
                                    .setTextAlignment(TextAlignment.LEFT)));
                        }
                        
                        System.out.println("Added item: " + itemName + " x" + quantity);
                    } catch (Exception e) {
                        System.err.println("Error processing item: " + e.getMessage());
                        // Add a placeholder row for the failed item
                        table.addCell(new Cell().add(new Paragraph("Error loading item")));
                        table.addCell(new Cell().add(new Paragraph("-").setTextAlignment(TextAlignment.CENTER)));
                        table.addCell(new Cell().add(new Paragraph("-").setTextAlignment(TextAlignment.RIGHT)));
                        table.addCell(new Cell().add(new Paragraph("-").setTextAlignment(TextAlignment.RIGHT)));
                    }
                }
            } else {
                // Add a "No items" row
                table.addCell(new Cell(1, 4).add(new Paragraph("No items found").setTextAlignment(TextAlignment.CENTER)));
            }
            
            document.add(table);
            
            document.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(10)
                    .setMarginBottom(10));
                    
            System.out.println("Items table added successfully");
        } catch (Exception e) {
            System.err.println("Error adding items table: " + e.getMessage());
        }
    }
    
    private void addPaymentDetails(Document document, Map<String, Object> payment) {
        try {
            document.add(new Paragraph("Payment Details")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));
            
            String paymentMethod = safeToString(payment.get("payment_method"), "N/A");
            document.add(new Paragraph("Payment Method: " + paymentMethod).setFontSize(12));
            
            String transactionDate = formatDate(payment.get("transaction_date"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            document.add(new Paragraph("Transaction Date: " + transactionDate).setFontSize(12));
            
            String paymentStatus = safeToString(payment.get("status"), "N/A");
            document.add(new Paragraph("Payment Status: " + paymentStatus).setFontSize(12));
            
            document.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(10)
                    .setMarginBottom(10));
                    
            System.out.println("Payment details added successfully");
        } catch (Exception e) {
            System.err.println("Error adding payment details: " + e.getMessage());
        }
    }
    
    private void addBillingSummary(Document document, Map<String, Object> order, Map<String, Object> payment) {
        try {
            document.add(new Paragraph("Billing Summary")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));
            
            // Get amounts from payment data first, then fallback to order data
            Double subtotal = safeToDouble(payment.get("subtotal"), safeToDouble(order.get("subtotal"), 0.0));
            Double tax = safeToDouble(payment.get("tax"), safeToDouble(order.get("tax"), 0.0));
            Double serviceCharge = safeToDouble(payment.get("service_charge"), safeToDouble(order.get("service_charge"), 0.0));
            Double totalAmount = safeToDouble(payment.get("grand_total"), safeToDouble(payment.get("final_amount"), safeToDouble(order.get("total_amount"), 0.0)));
            
            System.out.println("Billing amounts - Subtotal: " + subtotal + ", Tax: " + tax + ", Service: " + serviceCharge + ", Total: " + totalAmount);
            
            // Create billing table
            Table billingTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .useAllAvailableWidth()
                    .setMarginBottom(10);
            
            billingTable.addHeaderCell(new Cell().add(new Paragraph("Item").setBold()));
            billingTable.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold().setTextAlignment(TextAlignment.RIGHT)));
            
            billingTable.addCell(new Cell().add(new Paragraph("Subtotal")));
            billingTable.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", subtotal)).setTextAlignment(TextAlignment.RIGHT)));
            
            billingTable.addCell(new Cell().add(new Paragraph("Tax (5%)")));
            billingTable.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", tax)).setTextAlignment(TextAlignment.RIGHT)));
            
            billingTable.addCell(new Cell().add(new Paragraph("Service Charge (2%)")));
            billingTable.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", serviceCharge)).setTextAlignment(TextAlignment.RIGHT)));
            
            document.add(billingTable);
            
            // Add total
            document.add(new Paragraph("TOTAL PAID: " + String.format("%.0f MMK", totalAmount))
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(10));
                    
            System.out.println("Billing summary added successfully");
        } catch (Exception e) {
            System.err.println("Error adding billing summary: " + e.getMessage());
        }
    }
    
    private void addFooter(Document document) {
        try {
            document.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(20));
            
            document.add(new Paragraph("Thank you for your order!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setMarginTop(10));
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            document.add(new Paragraph("Generated on: " + dateFormat.format(new Date()))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setMarginTop(5));
                    
            System.out.println("Footer added successfully");
        } catch (Exception e) {
            System.err.println("Error adding footer: " + e.getMessage());
        }
    }
    
    /**
     * Generate a simple fallback PDF when the main generation fails
     */
    private byte[] generateFallbackPDF(Map<String, Object> order, Map<String, Object> payment) {
        System.out.println("Generating fallback PDF...");
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            document.add(new Paragraph("RESTAURANT POS SYSTEM - PAYMENT RECEIPT")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(20));
            
            document.add(new Paragraph("Order ID: " + safeToString(order.get("order_id"), "N/A")));
            document.add(new Paragraph("Payment Method: " + safeToString(payment.get("payment_method"), "N/A")));
            document.add(new Paragraph("Amount: " + String.format("%.0f MMK", safeToDouble(payment.get("grand_total"), 0.0))));
            document.add(new Paragraph("Status: " + safeToString(payment.get("status"), "N/A")));
            document.add(new Paragraph("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
            
            document.close();
            
            byte[] result = baos.toByteArray();
            System.out.println("Fallback PDF generated successfully, size: " + result.length + " bytes");
            return result;
            
        } catch (Exception e) {
            System.err.println("Failed to generate fallback PDF: " + e.getMessage());
            throw new RuntimeException("Unable to generate PDF receipt", e);
        }
    }
    
    // Utility methods for safe data conversion
    private String safeToString(Object value, String defaultValue) {
        if (value == null) return defaultValue;
        try {
            return value.toString();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    private Integer safeToInteger(Object value, Integer defaultValue) {
        if (value == null) return defaultValue;
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            System.err.println("Error converting to Integer: " + value + " - " + e.getMessage());
            return defaultValue;
        }
    }
    
    private Double safeToDouble(Object value, Double defaultValue) {
        if (value == null) return defaultValue;
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof java.math.BigDecimal) {
                return ((java.math.BigDecimal) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            System.err.println("Error converting to Double: " + value + " - " + e.getMessage());
            return defaultValue;
        }
    }
    
    private String formatDate(Object dateValue, SimpleDateFormat dateFormat) {
        if (dateValue == null) {
            return dateFormat.format(new Date());
        }
        
        try {
            if (dateValue instanceof String) {
                return (String) dateValue;
            } else if (dateValue instanceof java.util.Date) {
                return dateFormat.format((java.util.Date) dateValue);
            } else if (dateValue instanceof java.time.LocalDateTime) {
                return dateFormat.format(java.sql.Timestamp.valueOf((java.time.LocalDateTime) dateValue));
            } else {
                return String.valueOf(dateValue);
            }
        } catch (Exception e) {
            return dateFormat.format(new Date());
        }
    }
}
