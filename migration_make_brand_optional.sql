-- ============================================================
-- MIGRATION: Make Brand Optional in Products
-- Created: 2026-02-05
-- ============================================================

-- IMPORTANT: Run backup_before_brand_optional.sql FIRST!

-- Step 1: Make brand_id column nullable
ALTER TABLE products 
ALTER COLUMN brand_id DROP NOT NULL;

-- Step 2: Add comment to document the change
COMMENT ON COLUMN products.brand_id IS 'Brand ID (optional) - Products can be created without a brand';

-- Step 3: Verify the change
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'products' 
AND column_name IN ('brand_id', 'category_id');

-- Step 4: Check existing products
SELECT 
    COUNT(*) as total_products,
    COUNT(brand_id) as products_with_brand,
    COUNT(*) - COUNT(brand_id) as products_without_brand
FROM products;

-- ============================================================
-- Success Message
-- ============================================================

SELECT '✅ Brand is now optional! You can create products without brands.' as status;

-- ============================================================
-- ROLLBACK (if needed)
-- ============================================================

-- To make brand required again (only if NO products have NULL brand_id):
-- ALTER TABLE products ALTER COLUMN brand_id SET NOT NULL;
