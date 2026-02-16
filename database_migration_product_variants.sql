-- =====================================================
-- Product Variants Migration
-- =====================================================
-- Adds parent-child relationship for product variants
-- Supports: Code, Color, Size variants (all optional)
-- Backward compatible with existing products
-- =====================================================

-- Step 1: Add new columns to products table
ALTER TABLE products ADD COLUMN parent_product_id UUID REFERENCES products(id) ON DELETE CASCADE;
ALTER TABLE products ADD COLUMN is_variant BOOLEAN;  -- Allow NULL temporarily
ALTER TABLE products ADD COLUMN variant_code VARCHAR(50);
ALTER TABLE products ADD COLUMN variant_color VARCHAR(50);
ALTER TABLE products ADD COLUMN variant_size VARCHAR(50);

-- Step 1b: Set default values for existing rows
UPDATE products SET is_variant = false WHERE is_variant IS NULL;

-- Step 1c: Now add NOT NULL constraint and default
ALTER TABLE products ALTER COLUMN is_variant SET DEFAULT false;
ALTER TABLE products ALTER COLUMN is_variant SET NOT NULL;

-- Step 2: Add indexes for better performance
CREATE INDEX idx_products_parent ON products(parent_product_id);
CREATE INDEX idx_products_is_variant ON products(is_variant);
CREATE INDEX idx_products_variant_code ON products(variant_code);
CREATE INDEX idx_products_variant_color ON products(variant_color);
CREATE INDEX idx_products_variant_size ON products(variant_size);

-- Step 3: Add comments for documentation
COMMENT ON COLUMN products.parent_product_id IS 'References parent product if this is a variant';
COMMENT ON COLUMN products.is_variant IS 'True if this product is a variant of a parent product';
COMMENT ON COLUMN products.variant_code IS 'Variant code (e.g., Code 21, Code 23 for Cushion)';
COMMENT ON COLUMN products.variant_color IS 'Variant color (e.g., Blonde, Brown for Hair Color)';
COMMENT ON COLUMN products.variant_size IS 'Variant size (e.g., Big, Small)';

-- =====================================================
-- Verification Queries
-- =====================================================

-- View updated table structure
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'products' 
ORDER BY ordinal_position;

-- Count existing products (should be unchanged)
SELECT COUNT(*) as total_products FROM products;

-- Verify all existing products have NULL variant fields
SELECT COUNT(*) as non_variant_products 
FROM products 
WHERE parent_product_id IS NULL 
  AND is_variant = false;

-- =====================================================
-- Example Usage
-- =====================================================

-- Example 1: Create parent product (Cushion Felix)
/*
INSERT INTO products (
    name, category_id, cost_price, price, stock_quantity,
    is_variant, parent_product_id
) VALUES (
    'Cushion Felix', 
    'category-uuid', 
    15.00, 
    20.00, 
    0,  -- Parent has no stock
    false, 
    NULL
);
*/

-- Example 2: Create variant with code
/*
INSERT INTO products (
    name, category_id, cost_price, price, stock_quantity, barcode,
    parent_product_id, is_variant, variant_code
) VALUES (
    'Cushion Felix',
    'category-uuid',
    15.00,
    20.00,
    10,
    '111111',
    'parent-product-uuid',  -- Reference to parent
    true,
    '21'  -- Code 21
);
*/

-- Example 3: Create variant with color and size
/*
INSERT INTO products (
    name, category_id, cost_price, price, stock_quantity, barcode,
    parent_product_id, is_variant, variant_color, variant_size
) VALUES (
    'Hair Color',
    'category-uuid',
    10.00,
    15.00,
    15,
    '222222',
    'parent-product-uuid',
    true,
    'Blonde',
    'Big'
);
*/

-- =====================================================
-- Rollback (if needed)
-- =====================================================
/*
-- Remove indexes
DROP INDEX IF EXISTS idx_products_parent;
DROP INDEX IF EXISTS idx_products_is_variant;
DROP INDEX IF EXISTS idx_products_variant_code;
DROP INDEX IF EXISTS idx_products_variant_color;
DROP INDEX IF EXISTS idx_products_variant_size;

-- Remove columns
ALTER TABLE products DROP COLUMN IF EXISTS parent_product_id;
ALTER TABLE products DROP COLUMN IF EXISTS is_variant;
ALTER TABLE products DROP COLUMN IF EXISTS variant_code;
ALTER TABLE products DROP COLUMN IF EXISTS variant_color;
ALTER TABLE products DROP COLUMN IF EXISTS variant_size;
*/

-- =====================================================
-- Migration Complete! ✅
-- =====================================================
-- Next steps:
-- 1. Run this migration on your database
-- 2. Restart your application
-- 3. Test with the new API endpoints
-- =====================================================
