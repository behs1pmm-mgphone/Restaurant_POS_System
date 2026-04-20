-- Update payment table to include all required columns for PDF generation
-- This fixes the schema mismatch causing payment creation to fail

ALTER TABLE payment 
ADD COLUMN IF NOT EXISTS total_amount DECIMAL(10,2) AFTER final_amount,
ADD COLUMN IF NOT EXISTS subtotal DECIMAL(10,2) AFTER total_amount,
ADD COLUMN IF NOT EXISTS tax DECIMAL(10,2) AFTER subtotal,
ADD COLUMN IF NOT EXISTS service_charge DECIMAL(10,2) AFTER tax,
ADD COLUMN IF NOT EXISTS grand_total DECIMAL(10,2) AFTER service_charge;

-- Verify the table structure
DESCRIBE payment;
