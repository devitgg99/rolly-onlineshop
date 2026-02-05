-- ============================================================
-- DATA BACKUP SCRIPT - Before Making Brand Optional
-- Created: 2026-02-05
-- Purpose: Backup all data before schema changes
-- ============================================================

-- Step 1: Create backup schema
CREATE SCHEMA IF NOT EXISTS backup_20260205;

-- Step 2: Backup all brands
CREATE TABLE backup_20260205.brands AS
SELECT * FROM brands;

-- Step 3: Backup all categories
CREATE TABLE backup_20260205.categories AS
SELECT * FROM categories;

-- Step 4: Backup all products
CREATE TABLE backup_20260205.products AS
SELECT * FROM products;

-- Step 5: Backup product images
CREATE TABLE backup_20260205.product_images AS
SELECT * FROM product_images;

-- Step 6: Backup sales (if you want)
CREATE TABLE backup_20260205.sales AS
SELECT * FROM sales;

-- Step 7: Backup sale items
CREATE TABLE backup_20260205.sale_items AS
SELECT * FROM sale_items;

-- Step 8: Backup stock history
CREATE TABLE backup_20260205.stock_history AS
SELECT * FROM stock_history;

-- ============================================================
-- Verification Queries
-- ============================================================

-- Check backup counts
SELECT 'brands' as table_name, COUNT(*) as count FROM backup_20260205.brands
UNION ALL
SELECT 'categories', COUNT(*) FROM backup_20260205.categories
UNION ALL
SELECT 'products', COUNT(*) FROM backup_20260205.products
UNION ALL
SELECT 'product_images', COUNT(*) FROM backup_20260205.product_images
UNION ALL
SELECT 'sales', COUNT(*) FROM backup_20260205.sales
UNION ALL
SELECT 'sale_items', COUNT(*) FROM backup_20260205.sale_items
UNION ALL
SELECT 'stock_history', COUNT(*) FROM backup_20260205.stock_history;

-- View products with their brands (before change)
SELECT 
    p.id,
    p.name as product_name,
    b.name as brand_name,
    c.name as category_name,
    p.stock_quantity
FROM backup_20260205.products p
LEFT JOIN backup_20260205.brands b ON p.brand_id = b.id
LEFT JOIN backup_20260205.categories c ON p.category_id = c.id
ORDER BY p.created_at DESC
LIMIT 20;

-- ============================================================
-- Export to CSV (Optional - run these separately)
-- ============================================================

-- Export products to CSV
-- \COPY (SELECT * FROM products) TO 'products_backup_20260205.csv' WITH CSV HEADER;

-- Export brands to CSV
-- \COPY (SELECT * FROM brands) TO 'brands_backup_20260205.csv' WITH CSV HEADER;

-- ============================================================
-- ROLLBACK Script (if you need to restore)
-- ============================================================

-- To restore products:
-- TRUNCATE products CASCADE;
-- INSERT INTO products SELECT * FROM backup_20260205.products;

-- To restore brands:
-- TRUNCATE brands CASCADE;
-- INSERT INTO brands SELECT * FROM backup_20260205.brands;

-- ============================================================
-- Success Message
-- ============================================================

SELECT '✅ Backup completed successfully! Data saved to backup_20260205 schema.' as status;
