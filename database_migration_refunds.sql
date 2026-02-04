-- ============================================================
-- Database Migration: Refund & Return Management
-- Description: Creates tables for refund functionality
-- Date: 2026-02-05
-- ============================================================

-- Create refunds table
CREATE TABLE IF NOT EXISTS refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL,
    refund_amount DECIMAL(12, 2) NOT NULL,
    refund_method VARCHAR(50) NOT NULL,
    processed_by UUID,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_refund_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE RESTRICT,
    CONSTRAINT fk_refund_processed_by FOREIGN KEY (processed_by) REFERENCES app_user(user_id) ON DELETE SET NULL,
    
    -- Check constraints
    CONSTRAINT chk_refund_amount CHECK (refund_amount >= 0),
    CONSTRAINT chk_refund_method CHECK (refund_method IN ('CASH', 'CARD', 'STORE_CREDIT'))
);

-- Create refund_items table
CREATE TABLE IF NOT EXISTS refund_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    refund_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    reason TEXT,
    
    -- Foreign key constraints
    CONSTRAINT fk_refund_item_refund FOREIGN KEY (refund_id) REFERENCES refunds(id) ON DELETE CASCADE,
    CONSTRAINT fk_refund_item_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    
    -- Check constraints
    CONSTRAINT chk_refund_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_refund_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_refund_item_subtotal CHECK (subtotal >= 0)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_refunds_sale_id ON refunds(sale_id);
CREATE INDEX IF NOT EXISTS idx_refunds_created_at ON refunds(created_at);
CREATE INDEX IF NOT EXISTS idx_refunds_processed_by ON refunds(processed_by);
CREATE INDEX IF NOT EXISTS idx_refund_items_refund_id ON refund_items(refund_id);
CREATE INDEX IF NOT EXISTS idx_refund_items_product_id ON refund_items(product_id);

-- Add comments to tables and columns for documentation
COMMENT ON TABLE refunds IS 'Stores refund records for sales';
COMMENT ON COLUMN refunds.id IS 'Unique identifier for the refund';
COMMENT ON COLUMN refunds.sale_id IS 'Reference to the original sale';
COMMENT ON COLUMN refunds.refund_amount IS 'Total amount being refunded';
COMMENT ON COLUMN refunds.refund_method IS 'Method of refund: CASH, CARD, or STORE_CREDIT';
COMMENT ON COLUMN refunds.processed_by IS 'Admin who processed the refund';
COMMENT ON COLUMN refunds.notes IS 'Additional notes about the refund';
COMMENT ON COLUMN refunds.created_at IS 'Timestamp when refund was created';

COMMENT ON TABLE refund_items IS 'Stores individual items in a refund';
COMMENT ON COLUMN refund_items.id IS 'Unique identifier for the refund item';
COMMENT ON COLUMN refund_items.refund_id IS 'Reference to the parent refund';
COMMENT ON COLUMN refund_items.product_id IS 'Reference to the product being refunded';
COMMENT ON COLUMN refund_items.quantity IS 'Quantity of product being refunded';
COMMENT ON COLUMN refund_items.unit_price IS 'Unit price at time of original sale';
COMMENT ON COLUMN refund_items.subtotal IS 'Subtotal for this refund item (unit_price * quantity)';
COMMENT ON COLUMN refund_items.reason IS 'Reason for refunding this item';

-- ============================================================
-- Verification Queries (run these to verify the migration)
-- ============================================================

-- Check if tables were created
-- SELECT table_name FROM information_schema.tables 
-- WHERE table_schema = 'public' AND table_name IN ('refunds', 'refund_items');

-- Check table structure
-- \d refunds
-- \d refund_items

-- ============================================================
-- Rollback Script (if needed)
-- ============================================================

-- DROP TABLE IF EXISTS refund_items CASCADE;
-- DROP TABLE IF EXISTS refunds CASCADE;
