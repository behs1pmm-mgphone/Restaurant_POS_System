# Cashier Checkout with Automatic PDF Generation - Test Plan

## Implementation Summary

### What was implemented:
1. **Modified Checkout Endpoint**: `/cashier/orders/{id}/checkout` now automatically generates and downloads PDF
2. **Payment Database Integration**: PaymentService already handles complete payment record creation
3. **PDF Generation**: Automatic receipt generation with payment details

### How it works:

#### Before (Manual Process):
1. Cashier clicks checkout
2. Payment is processed
3. Cashier needs to manually click separate PDF download button
4. PDF is generated and downloaded

#### After (Automatic Process):
1. Cashier clicks checkout
2. Payment is processed and saved to database
3. PDF receipt is automatically generated
4. PDF is automatically downloaded to cashier's device

### Technical Changes Made:

**StaffDashboardController.java - checkoutOrder() method:**
- Changed return type from `Map<String, Object>` to `ResponseEntity<?>`
- Added automatic PDF generation after successful payment
- Set proper HTTP headers for PDF download
- Returns PDF content directly instead of JSON response

### Database Integration:
- PaymentService.createPayment() already handles:
  - Payment record creation in `payment` table
  - Sale report generation
  - Order status update to "Paid"
  - Proper tax and service charge calculations

### PDF Generation:
- Uses existing PdfService.generatePaymentReceipt() for paid orders
- Includes complete billing breakdown (subtotal, tax, service charge)
- Professional receipt format with restaurant branding

## Testing Steps:

### Prerequisites:
1. Start the Spring Boot application
2. Login as a cashier (role_id = 4)
3. Ensure there are orders in "Checkout" or "Pending" status

### Test Case 1: Successful Checkout with PDF
1. Navigate to cashier dashboard
2. Click "Checkout" on any available order
3. **Expected**: PDF should automatically download as "order_[ID]_receipt.pdf"
4. **Expected**: Order status should change to "Paid"
5. **Expected**: Payment record should be created in database

### Test Case 2: Error Handling
1. Try to checkout an already paid order
2. **Expected**: Error message returned (no PDF)
3. Try to checkout with invalid order ID
4. **Expected**: 404 error response

### Test Case 3: Payment Method Selection
1. Send POST request with different payment method:
```json
{
  "paymentMethod": "Credit Card"
}
```
2. **Expected**: PDF should show selected payment method

## API Endpoint Details:

**URL**: `POST /cashier/orders/{id}/checkout`
**Headers**: 
- Content-Type: application/json
- Session cookie for authenticated cashier

**Request Body** (optional):
```json
{
  "paymentMethod": "Cash"  // or "Credit Card", etc.
}
```

**Response**:
- **Success**: PDF file with headers for automatic download
- **Error**: JSON with error message

## Database Verification:

After successful checkout, verify:
```sql
SELECT * FROM payment WHERE order_id = [order_id];
SELECT * FROM `order` WHERE order_id = [order_id];
SELECT * FROM sale_report WHERE report_id = [payment_report_id];
```

## Benefits of This Implementation:

1. **Improved User Experience**: One-click checkout with automatic receipt
2. **Reduced Steps**: Eliminates separate PDF download action
3. **Professional Workflow**: Matches real-world POS systems
4. **Data Integrity**: Payment is saved before PDF generation
5. **Error Handling**: Proper validation and error responses

## Notes:

- The original manual PDF download endpoint remains available at `/cashier/orders/{id}/pdf`
- PaymentService handles all database operations with proper transaction management
- PDF generation includes complete order and payment details
- Implementation is backward compatible with existing frontend code
