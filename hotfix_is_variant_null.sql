-- =====================================================
-- HOTFIX: Update existing products with NULL is_variant
-- =====================================================
-- Run this IMMEDIATELY to fix the error!
-- =====================================================

-- Update all existing products to have is_variant = false
UPDATE products SET is_variant = false WHERE is_variant IS NULL;

-- Verify the fix
SELECT COUNT(*) as products_with_null_is_variant 
FROM products 
WHERE is_variant IS NULL;
-- Should return 0

-- =====================================================
-- Done! Restart your application now.
-- =====================================================
