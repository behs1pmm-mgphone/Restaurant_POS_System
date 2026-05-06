package com.spring.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

@Service
public class PdfService {

    /**
     * Bill မရှင်းခင် Order တင်ထားတာကိုပဲ Print ထုတ်ရန် (Payment Method မပါသေးပါ)
     */
    public byte[] generateOrderReceipt(Map<String, Object> order) throws IOException {
        return generateFullPDF(order, null);
    }

    /**
     * Bill ရှင်းပြီးမှ Payment Method ပါဝင်တဲ့ Receipt ထုတ်ရန်
     */
    public byte[] generatePaymentReceipt(Map<String, Object> order, Map<String, Object> payment) throws IOException {
        return generateFullPDF(order, payment);
    }

    /**
     * Core PDF Generation Logic
     */
    private byte[] generateFullPDF(Map<String, Object> order, Map<String, Object> payment) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addRestaurantHeader(document);
            addOrderDetails(document, order);
            addItemsTableAndBilling(document, order, payment);
            addFooter(document);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("PDF Generation Failed: " + e.getMessage());
        }
    }

    private void addRestaurantHeader(Document document) {
        document.add(new Paragraph("RESTAURANT POS SYSTEM")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold());
        document.add(new Paragraph("Order Receipt")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16)
                .setMarginBottom(20));
    }

    private void addOrderDetails(Document document, Map<String, Object> order) {
        document.add(new Paragraph("Order Details").setFontSize(14)
                .setBold()
                .setMarginBottom(5));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        document.add(new Paragraph("Order #: " + safeToString(order.get("order_id"), "N/A")));
        document.add(new Paragraph("Table: " + safeToString(order.get("table_number"), "N/A")));
        document.add(new Paragraph("Waiter: " + safeToString(order.get("waiter_name"), "N/A")));

        // Cashier နာမည်ထည့်ခြင်း

        document.add(new Paragraph("Cashier: " + safeToString(order.get("cashier_name"), "N/A")));
		/*
		 * String cashierName = safeToString(order.get("cashier_name"), "Admin");
		 * document.add(new Paragraph("Cashier: " + cashierName));
		 */

        document.add(new Paragraph("Date: " + formatDate(order.get("order_date"), dateFormat)));
        document.add(new Paragraph("Status: " + safeToString(order.get("order_status"), "N/A")));

        document.add(new LineSeparator(new SolidLine(1f)).setMarginTop(10).setMarginBottom(10));
    }

    private void addItemsTableAndBilling(Document document, Map<String, Object> order, Map<String, Object> payment) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 2, 2}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);

        // Header
        table.addHeaderCell(new Cell().add(new Paragraph("Item Name").setBold())
                .setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1f)));
        table.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold().setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1f)));
        table.addHeaderCell(new Cell().add(new Paragraph("Price").setBold().setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1f)));
        table.addHeaderCell(new Cell().add(new Paragraph("Total").setBold().setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1f)));

        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
        double subtotal = 0.0;

        if (items != null) {
            for (Map<String, Object> item : items) {
                double price = safeToDouble(item.get("unit_price"), 0.0);
                int qty = safeToInteger(item.get("quantity"), 0);
                double total = price * qty;
                subtotal += total;

                table.addCell(new Cell().add(new Paragraph(safeToString(item.get("menu_name"), "Item"))).setBorder(Border.NO_BORDER));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(qty)).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
                table.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", price)).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
                table.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", total)).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
            }
        }
        document.add(table);

        // Billing Summary Section
        addBillingSummary(document, subtotal, payment);
    }

    private void addBillingSummary(Document document, double subtotal, Map<String, Object> payment) {
        double tax = subtotal * 0.05; // 5%
        double serviceCharge  = subtotal * 0.02;          // 2%
        double grandTotal = subtotal +tax +serviceCharge;

        Table billingTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth()
                .setMarginTop(10);

        billingTable.addCell(new Cell().add(new Paragraph("Sub-total:")).setBorder(Border.NO_BORDER));
        billingTable.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", subtotal)).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));

        billingTable.addCell(new Cell().add(new Paragraph("Service Charge (2%):")).setBorder(Border.NO_BORDER));
        billingTable.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", serviceCharge)).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));

        billingTable.addCell(new Cell().add(new Paragraph("Tax (5%):")).setBorder(Border.NO_BORDER));
        billingTable.addCell(new Cell().add(new Paragraph(String.format("%.0f MMK", tax)).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));

        // --- Payment Method ကို ဒီနေရာမှာ ပြပေးမှာပါ ---
		/*
		 * if (payment != null) { String method =
		 * safeToString(payment.get("payment_method"), "Cash"); billingTable.addCell(new
		 * Cell().add(new
		 * Paragraph("Payment Method:").setItalic()).setBorder(Border.NO_BORDER));
		 * billingTable.addCell(new Cell().add(new
		 * Paragraph(method).setItalic().setTextAlignment(TextAlignment.RIGHT)).
		 * setBorder(Border.NO_BORDER)); }
		 */

     // PdfService.java ထဲက addBillingSummary သို့မဟုတ် logic နေရာမှာ အစားထိုးပါ
        if (payment != null) {
            // စာလုံးအကြီး အသေး နှစ်မျိုးလုံးကို စစ်မယ်
            Object method = payment.get("payment_method");
            if (method == null) method = payment.get("PAYMENT_METHOD");

            String displayMethod = (method != null) ? method.toString() : "Cash";

            billingTable.addCell(new Cell().add(new Paragraph("Payment Method:")).setItalic().setBorder(Border.NO_BORDER));
            billingTable.addCell(new Cell().add(new Paragraph(displayMethod)).setItalic().setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
        }
        document.add(billingTable);
        document.add(new LineSeparator(new SolidLine(1f)).setMarginTop(5));

        document.add(new Paragraph("TOTAL: " + String.format("%.0f MMK", grandTotal))
                .setBold().setFontSize(16).setTextAlignment(TextAlignment.RIGHT));
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\nThank you for your order!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12));

        SimpleDateFormat footerFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        document.add(new Paragraph("Generated on: " + footerFormat.format(new Date()))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));
    }

    // --- Helper Methods ---
    private String safeToString(Object v, String d) { return v == null ? d : v.toString(); }

    private int safeToInteger(Object v, int d) {
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return d; }
    }

    private double safeToDouble(Object v, double d) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return d; }
    }

    private String formatDate(Object d, SimpleDateFormat f) {
        if (d instanceof Date) return f.format((Date) d);
        return d == null ? f.format(new Date()) : d.toString();
    }
}