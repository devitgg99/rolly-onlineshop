# 🚀 Product Variants - Quick Reference Card

## 📝 Essential Info

### Database Migration
```bash
psql -U user -d database -f database_migration_product_variants.sql
```

### Restart App
```bash
./gradlew bootRun
```

---

## 🔌 API Endpoints Quick Reference

### Create Product
```bash
POST /api/v1/products
```

**Regular Product:**
```json
{
  "name": "Shampoo",
  "categoryId": "uuid",
  "costPrice": 8.00,
  "price": 10.00,
  "stockQuantity": 50
}
```

**Parent Product:**
```json
{
  "name": "Cushion Felix",
  "categoryId": "uuid",
  "costPrice": 15.00,
  "price": 20.00,
  "stockQuantity": 0
}
```

**Variant Product:**
```json
{
  "name": "Cushion Felix",
  "categoryId": "uuid",
  "parentProductId": "parent-uuid",
  "isVariant": true,
  "variantCode": "21",
  "costPrice": 15.00,
  "price": 20.00,
  "stockQuantity": 10,
  "barcode": "111111"
}
```

### Get Variants
```bash
GET /api/v1/products/{parentId}/variants
```

### Get Grouped Products
```bash
GET /api/v1/products/admin/grouped?page=0&size=20
```

### Check Can Delete
```bash
GET /api/v1/products/admin/{id}/can-delete
```

---

## 📊 New Database Columns

| Column | Type | Description |
|--------|------|-------------|
| `parent_product_id` | UUID | Parent reference |
| `is_variant` | Boolean | Is this a variant? |
| `variant_code` | VARCHAR(50) | Code (21, 23) |
| `variant_color` | VARCHAR(50) | Color (Blonde) |
| `variant_size` | VARCHAR(50) | Size (Big) |

---

## 💡 Key Concepts

### Parent Product
- Template product
- Has no stock (`stockQuantity: 0`)
- `isVariant: false`
- `parentProductId: null`

### Variant Product
- Actual sellable product
- Has stock
- `isVariant: true`
- `parentProductId: <parent-uuid>`
- Has variant fields (code/color/size)

### Standalone Product
- Regular product without variants
- `isVariant: false`
- `parentProductId: null`
- Has stock

---

## 🎨 Display Examples

### Flat View
```
Shampoo | Stock: 50 | $10
Cushion Felix - Code 21 | Stock: 10 | $20
Cushion Felix - Code 23 | Stock: 5 | $20
```

### Grouped View (Collapsed)
```
Shampoo | Stock: 50 | $10
📦 Cushion Felix [+] | 3 variants
```

### Grouped View (Expanded)
```
Shampoo | Stock: 50 | $10
📦 Cushion Felix [-]
  ├─ Code 21 | Stock: 10 | $20
  ├─ Code 23 | Stock: 5 | $20
  └─ Code 25 | Stock: 8 | $20
```

---

## ✅ Workflow

### Creating Variants:

1. **Create Parent**
   ```
   POST /products
   {
     "name": "Cushion Felix",
     "price": 20,
     "stockQuantity": 0
   }
   → Returns parent UUID
   ```

2. **Create Variants**
   ```
   POST /products
   {
     "name": "Cushion Felix",
     "parentProductId": "parent-uuid",
     "isVariant": true,
     "variantCode": "21",
     "stockQuantity": 10
   }
   ```

3. **Display**
   ```
   GET /products/admin/grouped
   → Shows parent with variants
   ```

---

## 🔍 Response Examples

### Variant Info Response
```json
{
  "id": "uuid",
  "variantCode": "21",
  "variantColor": null,
  "variantSize": null,
  "stockQuantity": 10,
  "price": 20.00,
  "profit": 5.00,
  "barcode": "111111"
}
```

### Grouped Product Response
```json
{
  "id": "uuid",
  "name": "Cushion Felix",
  "isVariant": false,
  "hasVariants": true,  // ← Has children
  "stockQuantity": 0
}
```

---

## 🎯 Common Use Cases

### Use Case 1: Cushion with Codes
```
Parent: Cushion Felix
Variants:
  - variantCode: "21"
  - variantCode: "23"
  - variantCode: "25"
```

### Use Case 2: Hair Color
```
Parent: Hair Color
Variants:
  - variantColor: "Blonde"
  - variantColor: "Brown"
  - variantColor: "Black"
```

### Use Case 3: Hair Color with Size
```
Parent: Hair Color
Variants:
  - variantColor: "Blonde", variantSize: "Big"
  - variantColor: "Blonde", variantSize: "Small"
  - variantColor: "Brown", variantSize: "Big"
```

---

## 📋 Testing Checklist

**Backend:**
- [ ] Migration successful
- [ ] Create parent product
- [ ] Create variant
- [ ] Get variants
- [ ] Grouped products

**Frontend:**
- [ ] Display grouped view
- [ ] Expand/collapse
- [ ] Create variant form
- [ ] Variant labels

---

## 📚 Documentation Files

1. **`PRODUCT_VARIANTS_FRONTEND_GUIDE.md`**
   - Complete frontend integration
   - React & Vue examples
   - UI mockups

2. **`PRODUCT_VARIANTS_SUMMARY.md`**
   - Implementation summary
   - Files changed
   - Quick start

3. **`database_migration_product_variants.sql`**
   - Database migration script

---

## 💬 Quick Tips

✅ **Parent has no stock** - Stock is in variants  
✅ **Variants need parent** - Set `parentProductId`  
✅ **Use what you need** - code, color, size are optional  
✅ **Backward compatible** - Existing products work  
✅ **Each variant unique** - Own barcode, stock, price  

---

## 🎉 You're Ready!

1. Run migration
2. Restart app
3. Test API
4. Build UI
5. Start selling!

**Need help? Read `PRODUCT_VARIANTS_FRONTEND_GUIDE.md`**

---

**Happy coding! 🚀✨**
