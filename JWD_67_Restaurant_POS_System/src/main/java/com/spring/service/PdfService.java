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

    public byte[] generateOrderReceipt(Map<String, Object> order) throws IOException {
        System.out.println("=== DEBUG: PDF Generation Started ===");
        System.out.println("Order data: " + order);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
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

    public byte[] generatePaymentReceipt(Map<String, Object> order, Map<String, Object> payment) throws IOException {
        System.out.println("=== DEBUG: Payment PDF Generation Started ===");
        System.out.println("Order data: " + order);
        System.out.println("Payment data: " + payment);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            System.out.println("PDF document created successfully");
        
        // Add restaurant header
        document.add(new Paragraph("RESTAURANT POS SYSTEM")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold());
        
        document.add(new Paragraph("Payment Receipt")
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
                orderDate = (String) orderDateObj;
            } else if (orderDateObj instanceof java.util.Date) {
                orderDate = dateFormat.format((java.util.Date) orderDateObj);
            } else {
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
        
        // Add payment details
        document.add(new Paragraph("Payment Details")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));
        
        if (payment != null) {
            document.add(new Paragraph("Payment Method: " + (payment.get("payment_method") != null ? payment.get("payment_method") : "N/A"))
                    .setFontSize(12));
            
            Object transactionDateObj = payment.get("transaction_date");
            String transactionDate = "";
            if (transactionDateObj != null) {
                if (transactionDateObj instanceof java.time.LocalDateTime) {
                    transactionDate = dateFormat.format(java.sql.Timestamp.valueOf((java.time.LocalDateTime) transactionDateObj));
                } else {
                    transactionDate = String.valueOf(transactionDateObj);
                }
            }
            document.add(new Paragraph("Transaction Date: " + transactionDate)
                    .setFontSize(12));
            document.add(new Paragraph("Payment Status: " + (payment.get("status") != null ? payment.get("status") : "N/A"))
                    .setFontSize(12));
        }
        
        // Add billing breakdown
        document.add(new Paragraph("Billing Summary")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));
        
        // Calculate subtotal
        Object totalAmountObj = order.get("total_amount");
        Object taxObj = order.get("tax");
        Object serviceChargeObj = order.get("service_charge");
        
        Double totalAmount = totalAmountObj != null ? 
            ((java.math.BigDecimal) totalAmountObj).doubleValue() : 0.0;
        Double tax = taxObj != null ? 
            ((java.math.BigDecimal) taxObj).doubleValue() : 0.0;
        Double serviceCharge = serviceChargeObj != null ? 
            ((java.math.BigDecimal) serviceChargeObj).doubleValue() : 0.0;
        Double subtotal = totalAmount - tax - serviceCharge;
        
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
        
            document.close();
            
            byte[] result = baos.toByteArray();
            System.out.println("Payment PDF completed successfully, size: " + result.length + " bytes");
            return result;
            
        } catch (Exception e) {
            System.err.println("ERROR in payment PDF generation: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to generate payment PDF: " + e.getMessage(), e);
        }
    }
}
