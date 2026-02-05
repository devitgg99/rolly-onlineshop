-- ============================================================
-- MIGRATION: Simplify Product/Category Creation
-- Created: 2026-02-05
-- Purpose: Make brand optional, confirm category image optional
-- ============================================================

-- IMPORTANT: Run backup_before_brand_optional.sql FIRST!

BEGIN;

-- ============================================================
-- Step 1: Make brand_id optional in products
-- ============================================================

ALTER TABLE products 
ALTER COLUMN brand_id DROP NOT NULL;

COMMENT ON COLUMN products.brand_id IS 
'Brand ID (optional) - Products can be created without a brand';

-- ============================================================
-- Step 2: Verify category image_url is already optional
-- ============================================================

-- Check if image_url is nullable (should already be)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'categories' 
        AND column_name = 'image_url'
        AND is_nullable = 'YES'
    ) THEN
        RAISE NOTICE '✅ Category image_url is already optional!';
    ELSE
        -- Make it optional if not already
        ALTER TABLE categories ALTER COLUMN image_url DROP NOT NULL;
        RAISE NOTICE '✅ Made category image_url optional!';
    END IF;
END $$;

-- ============================================================
-- Step 3: Verify Changes
-- ============================================================

SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name IN ('products', 'categories')
AND column_name IN ('brand_id', 'image_url')
ORDER BY table_name, column_name;

-- ============================================================
-- Step 4: Check Current Data
-- ============================================================

-- Count products with/without brands
SELECT 
    'Products' as type,
    COUNT(*) as total,
    COUNT(brand_id) as with_brand,
    COUNT(*) - COUNT(brand_id) as without_brand
FROM products;

-- Count categories with/without images
SELECT 
    'Categories' as type,
    COUNT(*) as total,
    COUNT(image_url) as with_image,
    COUNT(*) - COUNT(image_url) as without_image
FROM categories;

-- ============================================================
-- Step 5: Test Creating Product Without Brand
-- ============================================================

-- This is just documentation - run via API:
-- POST /api/v1/products
-- {
--   "name": "Test Product",
--   "costPrice": 10.00,
--   "price": 20.00,
--   "stockQuantity": 50,
--   "categoryId": "your-category-uuid"
--   // No brandId needed!
-- }

-- ============================================================
-- Step 6: Test Creating Category Without Image
-- ============================================================

-- This is just documentation - run via API:
-- POST /api/v1/categories
-- {
--   "name": "Test Category",
--   "description": "Category without image"
--   // No imageUrl needed!
-- }

COMMIT;

-- ============================================================
-- Success Message
-- ============================================================

SELECT '✅ Migration complete! Brand and category image are now optional!' as status;

-- ============================================================
-- ROLLBACK (if needed - only run if something goes wrong)
-- ============================================================

-- To revert (only if NO products have NULL brand_id):
-- BEGIN;
-- ALTER TABLE products ALTER COLUMN brand_id SET NOT NULL;
-- COMMIT;
