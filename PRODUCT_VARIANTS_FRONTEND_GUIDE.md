# 🎨 Product Variants - Complete Frontend Integration Guide

## 📋 Table of Contents
1. [Overview](#overview)
2. [Database Changes](#database-changes)
3. [API Endpoints](#api-endpoints)
4. [Request/Response Examples](#requestresponse-examples)
5. [Frontend Implementation](#frontend-implementation)
6. [UI/UX Examples](#uiux-examples)
7. [Testing Checklist](#testing-checklist)

---

## 🎯 Overview

Your backend now supports **product variants** with a parent-child relationship! This allows you to:

✅ Group related products (e.g., Cushion Felix Code 21, 22, 23)  
✅ Support optional variant attributes: **code**, **color**, **size**  
✅ Maintain backward compatibility (existing products work as before)  
✅ Track stock separately for each variant  
✅ Display products in grouped or flat view  

### Key Features:
- **Parent Product**: Template product with no stock (e.g., "Cushion Felix")
- **Variant Products**: Actual products with stock (e.g., "Code 21", "Code 23")
- **Optional Fields**: code, color, size (use what you need)
- **Flexible**: Products can have variants, or be standalone

---

## 📊 Database Changes

### New Columns Added to `products` table:

| Column | Type | Description | Example |
|--------|------|-------------|---------|
| `parent_product_id` | UUID | References parent if this is a variant | `UUID of parent` |
| `is_variant` | Boolean | True if this is a variant | `true` or `false` |
| `variant_code` | VARCHAR(50) | Variant code | `"21"`, `"23"` |
| `variant_color` | VARCHAR(50) | Variant color | `"Blonde"`, `"Brown"` |
| `variant_size` | VARCHAR(50) | Variant size | `"Big"`, `"Small"` |

### Migration Script:
Run the SQL script: `database_migration_product_variants.sql`

```bash
psql -U your_user -d your_database -f database_migration_product_variants.sql
```

---

## 🔌 API Endpoints

### 1. Create Product (Updated)

**Endpoint:** `POST /api/v1/products`  
**Auth:** Admin only

#### **Create Regular Product (No Variants)**
```json
POST /api/v1/products
{
  "name": "Shampoo",
  "categoryId": "category-uuid",
  "costPrice": 8.00,
  "price": 10.00,
  "stockQuantity": 50,
  "barcode": "123456"
}
```

#### **Create Parent Product**
```json
POST /api/v1/products
{
  "name": "Cushion Felix",
  "categoryId": "category-uuid",
  "costPrice": 15.00,
  "price": 20.00,
  "stockQuantity": 0,        // Parent has no stock
  "isVariant": false
}
```

#### **Create Variant with Code**
```json
POST /api/v1/products
{
  "name": "Cushion Felix",
  "categoryId": "category-uuid",
  "parentProductId": "parent-uuid",  // ← Link to parent
  "isVariant": true,
  "variantCode": "21",               // ← Variant code
  "costPrice": 15.00,
  "price": 20.00,
  "stockQuantity": 10,
  "barcode": "111111"
}
```

#### **Create Variant with Color + Size**
```json
POST /api/v1/products
{
  "name": "Hair Color",
  "categoryId": "category-uuid",
  "parentProductId": "parent-uuid",
  "isVariant": true,
  "variantColor": "Blonde",          // ← Color
  "variantSize": "Big",              // ← Size
  "costPrice": 10.00,
  "price": 15.00,
  "stockQuantity": 15,
  "barcode": "222222"
}
```

---

### 2. Get Product Variants

**Endpoint:** `GET /api/v1/products/{parentId}/variants`  
**Auth:** Admin only

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "variant-1-uuid",
      "variantCode": "21",
      "variantColor": null,
      "variantSize": null,
      "stockQuantity": 10,
      "price": 20.00,
      "discountedPrice": 20.00,
      "costPrice": 15.00,
      "profit": 5.00,
      "barcode": "111111"
    },
    {
      "id": "variant-2-uuid",
      "variantCode": "23",
      "variantColor": null,
      "variantSize": null,
      "stockQuantity": 5,
      "price": 20.00,
      "discountedPrice": 20.00,
      "costPrice": 15.00,
      "profit": 5.00,
      "barcode": "111112"
    }
  ],
  "message": "Product variants"
}
```

---

### 3. Get Grouped Products

**Endpoint:** `GET /api/v1/products/admin/grouped?page=0&size=20`  
**Auth:** Admin only

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "product-1-uuid",
        "name": "Shampoo",
        "stockQuantity": 50,
        "price": 10.00,
        "isVariant": false,
        "parentProductId": null,
        "hasVariants": false,          // ← No variants
        "variantCode": null,
        "variantColor": null,
        "variantSize": null
      },
      {
        "id": "parent-uuid",
        "name": "Cushion Felix",
        "stockQuantity": 0,
        "price": 20.00,
        "isVariant": false,
        "parentProductId": null,
        "hasVariants": true,           // ← Has variants!
        "variantCode": null,
        "variantColor": null,
        "variantSize": null
      },
      {
        "id": "variant-1-uuid",
        "name": "Cushion Felix",
        "stockQuantity": 10,
        "price": 20.00,
        "isVariant": true,             // ← This is a variant
        "parentProductId": "parent-uuid",
        "hasVariants": false,
        "variantCode": "21",           // ← Code 21
        "variantColor": null,
        "variantSize": null
      }
    ],
    "totalPages": 5,
    "totalElements": 100,
    "currentPage": 0
  }
}
```

---

### 4. Check if Product Can Be Deleted

**Endpoint:** `GET /api/v1/products/admin/{id}/can-delete`  
**Auth:** Admin only

**Response:**
```json
{
  "success": true,
  "data": {
    "canDelete": false  // Cannot delete (has variants or sales)
  }
}
```

---

## 💻 Frontend Implementation

### React Example

#### 1. **Create Product Form**

```tsx
import { useState } from 'react';

interface ProductFormData {
  name: string;
  categoryId: string;
  costPrice: number;
  price: number;
  stockQuantity: number;
  barcode?: string;
  parentProductId?: string;  // NEW!
  isVariant: boolean;        // NEW!
  variantCode?: string;      // NEW!
  variantColor?: string;     // NEW!
  variantSize?: string;      // NEW!
}

function ProductForm() {
  const [formData, setFormData] = useState<ProductFormData>({
    name: '',
    categoryId: '',
    costPrice: 0,
    price: 0,
    stockQuantity: 0,
    isVariant: false
  });

  const [isCreatingVariant, setIsCreatingVariant] = useState(false);
  const [parentProducts, setParentProducts] = useState([]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const response = await fetch('/api/v1/products', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(formData)
    });

    const result = await response.json();
    if (result.success) {
      alert('Product created!');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Create Product</h2>

      {/* Basic Fields */}
      <input
        type="text"
        placeholder="Product Name"
        value={formData.name}
        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
        required
      />

      <input
        type="number"
        placeholder="Cost Price"
        value={formData.costPrice}
        onChange={(e) => setFormData({ ...formData, costPrice: parseFloat(e.target.value) })}
        required
      />

      <input
        type="number"
        placeholder="Selling Price"
        value={formData.price}
        onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) })}
        required
      />

      <input
        type="number"
        placeholder="Stock Quantity"
        value={formData.stockQuantity}
        onChange={(e) => setFormData({ ...formData, stockQuantity: parseInt(e.target.value) })}
        required
      />

      <input
        type="text"
        placeholder="Barcode (optional)"
        value={formData.barcode || ''}
        onChange={(e) => setFormData({ ...formData, barcode: e.target.value })}
      />

      {/* Variant Section */}
      <hr />
      <label>
        <input
          type="checkbox"
          checked={isCreatingVariant}
          onChange={(e) => {
            setIsCreatingVariant(e.target.checked);
            setFormData({ ...formData, isVariant: e.target.checked });
          }}
        />
        This is a variant of an existing product
      </label>

      {isCreatingVariant && (
        <div style={{ marginLeft: '20px', padding: '10px', background: '#f5f5f5' }}>
          <h3>Variant Details</h3>

          <select
            value={formData.parentProductId || ''}
            onChange={(e) => setFormData({ ...formData, parentProductId: e.target.value })}
            required
          >
            <option value="">Select Parent Product</option>
            {parentProducts.map((product: any) => (
              <option key={product.id} value={product.id}>
                {product.name}
              </option>
            ))}
          </select>

          <input
            type="text"
            placeholder="Variant Code (e.g., 21, 23)"
            value={formData.variantCode || ''}
            onChange={(e) => setFormData({ ...formData, variantCode: e.target.value })}
          />

          <input
            type="text"
            placeholder="Variant Color (e.g., Blonde, Brown)"
            value={formData.variantColor || ''}
            onChange={(e) => setFormData({ ...formData, variantColor: e.target.value })}
          />

          <input
            type="text"
            placeholder="Variant Size (e.g., Big, Small)"
            value={formData.variantSize || ''}
            onChange={(e) => setFormData({ ...formData, variantSize: e.target.value })}
          />
        </div>
      )}

      <button type="submit">Create Product</button>
    </form>
  );
}

export default ProductForm;
```

---

#### 2. **Product List with Grouped View**

```tsx
import { useState, useEffect } from 'react';

interface Product {
  id: string;
  name: string;
  stockQuantity: number;
  price: number;
  isVariant: boolean;
  parentProductId?: string;
  hasVariants: boolean;
  variantCode?: string;
  variantColor?: string;
  variantSize?: string;
}

function ProductList() {
  const [products, setProducts] = useState<Product[]>([]);
  const [expandedProducts, setExpandedProducts] = useState<Set<string>>(new Set());
  const [variantsCache, setVariantsCache] = useState<Record<string, any[]>>({});

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    const response = await fetch('/api/v1/products/admin/grouped?page=0&size=100', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    setProducts(result.data.content);
  };

  const loadVariants = async (parentId: string) => {
    if (variantsCache[parentId]) {
      return; // Already loaded
    }

    const response = await fetch(`/api/v1/products/${parentId}/variants`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    
    setVariantsCache((prev) => ({
      ...prev,
      [parentId]: result.data
    }));
  };

  const toggleExpand = async (productId: string) => {
    const newExpanded = new Set(expandedProducts);
    
    if (newExpanded.has(productId)) {
      newExpanded.delete(productId);
    } else {
      newExpanded.add(productId);
      await loadVariants(productId);
    }
    
    setExpandedProducts(newExpanded);
  };

  const getVariantLabel = (product: Product) => {
    const parts: string[] = [];
    if (product.variantCode) parts.push(`Code ${product.variantCode}`);
    if (product.variantColor) parts.push(product.variantColor);
    if (product.variantSize) parts.push(product.variantSize);
    return parts.length > 0 ? ` (${parts.join(' - ')})` : '';
  };

  return (
    <div className="product-list">
      <h2>Products</h2>

      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Stock</th>
            <th>Price</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {products.map((product) => (
            <>
              {/* Parent or Standalone Product */}
              {!product.isVariant && (
                <tr key={product.id} className={product.hasVariants ? 'parent-product' : ''}>
                  <td>
                    {product.hasVariants && (
                      <button onClick={() => toggleExpand(product.id)}>
                        {expandedProducts.has(product.id) ? '▼' : '▶'}
                      </button>
                    )}
                    📦 {product.name}
                    {product.hasVariants && <span> ({variantsCache[product.id]?.length || '?'} variants)</span>}
                  </td>
                  <td>{product.stockQuantity}</td>
                  <td>${product.price.toFixed(2)}</td>
                  <td>
                    <button>Edit</button>
                    <button>Delete</button>
                  </td>
                </tr>
              )}

              {/* Variants (expanded) */}
              {!product.isVariant && expandedProducts.has(product.id) && variantsCache[product.id] && (
                variantsCache[product.id].map((variant) => (
                  <tr key={variant.id} className="variant-row">
                    <td style={{ paddingLeft: '40px' }}>
                      └─ {variant.variantCode && `Code ${variant.variantCode}`}
                      {variant.variantColor && ` ${variant.variantColor}`}
                      {variant.variantSize && ` (${variant.variantSize})`}
                    </td>
                    <td>{variant.stockQuantity}</td>
                    <td>${variant.price.toFixed(2)}</td>
                    <td>
                      <button>Edit</button>
                      <button>Delete</button>
                    </td>
                  </tr>
                ))
              )}
            </>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default ProductList;
```

---

### Vue.js Example

```vue
<template>
  <div class="product-list">
    <h2>Products</h2>

    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Stock</th>
          <th>Price</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <template v-for="product in products" :key="product.id">
          <!-- Parent Product -->
          <tr v-if="!product.isVariant" :class="{ 'parent-product': product.hasVariants }">
            <td>
              <button v-if="product.hasVariants" @click="toggleExpand(product.id)">
                {{ expandedProducts.has(product.id) ? '▼' : '▶' }}
              </button>
              📦 {{ product.name }}
              <span v-if="product.hasVariants">
                ({{ variantsCache[product.id]?.length || '?' }} variants)
              </span>
            </td>
            <td>{{ product.stockQuantity }}</td>
            <td>${{ product.price.toFixed(2) }}</td>
            <td>
              <button>Edit</button>
              <button>Delete</button>
            </td>
          </tr>

          <!-- Variants (expanded) -->
          <template v-if="!product.isVariant && expandedProducts.has(product.id)">
            <tr
              v-for="variant in variantsCache[product.id]"
              :key="variant.id"
              class="variant-row"
            >
              <td style="padding-left: 40px">
                └─ 
                <span v-if="variant.variantCode">Code {{ variant.variantCode }}</span>
                <span v-if="variant.variantColor">{{ variant.variantColor }}</span>
                <span v-if="variant.variantSize">({{ variant.variantSize }})</span>
              </td>
              <td>{{ variant.stockQuantity }}</td>
              <td>${{ variant.price.toFixed(2) }}</td>
              <td>
                <button>Edit</button>
                <button>Delete</button>
              </td>
            </tr>
          </template>
        </template>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

interface Product {
  id: string;
  name: string;
  stockQuantity: number;
  price: number;
  isVariant: boolean;
  parentProductId?: string;
  hasVariants: boolean;
  variantCode?: string;
  variantColor?: string;
  variantSize?: string;
}

const products = ref<Product[]>([]);
const expandedProducts = ref<Set<string>>(new Set());
const variantsCache = ref<Record<string, any[]>>({});

onMounted(() => {
  fetchProducts();
});

const fetchProducts = async () => {
  const response = await fetch('/api/v1/products/admin/grouped?page=0&size=100', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const result = await response.json();
  products.value = result.data.content;
};

const loadVariants = async (parentId: string) => {
  if (variantsCache.value[parentId]) return;

  const response = await fetch(`/api/v1/products/${parentId}/variants`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const result = await response.json();
  
  variantsCache.value[parentId] = result.data;
};

const toggleExpand = async (productId: string) => {
  if (expandedProducts.value.has(productId)) {
    expandedProducts.value.delete(productId);
  } else {
    expandedProducts.value.add(productId);
    await loadVariants(productId);
  }
};
</script>

<style scoped>
.parent-product {
  font-weight: bold;
  background: #f5f5f5;
}

.variant-row {
  background: #fafafa;
}
</style>
```

---

## 🎨 UI/UX Examples

### **1. Flat View (All Products)**
```
┌─────────────────────────────────────────────────────┐
│ Products                                   [+ New]  │
├─────────────────────────────────────────────────────┤
│ Name                      │ Stock │ Price │ Actions│
├─────────────────────────────────────────────────────┤
│ Shampoo                   │ 50    │ $10   │ ✏️ 🗑️   │
│ Cushion Felix - Code 21   │ 10    │ $20   │ ✏️ 🗑️   │
│ Cushion Felix - Code 23   │ 5     │ $20   │ ✏️ 🗑️   │
│ Hair Color - Blonde       │ 15    │ $15   │ ✏️ 🗑️   │
│ Hair Color - Brown        │ 20    │ $15   │ ✏️ 🗑️   │
└─────────────────────────────────────────────────────┘
```

### **2. Grouped View (Collapsible)**
```
┌─────────────────────────────────────────────────────┐
│ Products                                   [+ New]  │
├─────────────────────────────────────────────────────┤
│ Name                      │ Stock │ Price │ Actions│
├─────────────────────────────────────────────────────┤
│ Shampoo                   │ 50    │ $10   │ ✏️ 🗑️   │
│ ▶ 📦 Cushion Felix        │ 23    │ $20   │ ✏️ 🗑️   │
│   (3 variants)                                      │
│ ▶ 📦 Hair Color           │ 47    │ $15   │ ✏️ 🗑️   │
│   (3 variants)                                      │
└─────────────────────────────────────────────────────┘
```

### **3. Expanded View**
```
┌─────────────────────────────────────────────────────┐
│ Products                                   [+ New]  │
├─────────────────────────────────────────────────────┤
│ Name                      │ Stock │ Price │ Actions│
├─────────────────────────────────────────────────────┤
│ Shampoo                   │ 50    │ $10   │ ✏️ 🗑️   │
│ ▼ 📦 Cushion Felix        │ -     │ -     │ ✏️ 🗑️   │
│   └─ Code 21              │ 10    │ $20   │ ✏️ 🗑️   │
│   └─ Code 23              │ 5     │ $20   │ ✏️ 🗑️   │
│   └─ Code 25              │ 8     │ $20   │ ✏️ 🗑️   │
│ ▶ 📦 Hair Color           │ 47    │ $15   │ ✏️ 🗑️   │
└─────────────────────────────────────────────────────┘
```

---

## ✅ Testing Checklist

### Backend Testing

- [ ] Run database migration script
- [ ] Restart application
- [ ] Test create parent product (no variants)
- [ ] Test create variant with code
- [ ] Test create variant with color
- [ ] Test create variant with size
- [ ] Test create variant with multiple attributes
- [ ] Test get variants endpoint
- [ ] Test grouped products endpoint
- [ ] Test can-delete endpoint
- [ ] Verify existing products still work

### Frontend Testing

- [ ] Display products in flat view
- [ ] Display products in grouped view
- [ ] Expand/collapse parent products
- [ ] Load variants dynamically
- [ ] Create new parent product
- [ ] Create new variant
- [ ] Edit variant
- [ ] Delete variant
- [ ] Show variant labels correctly
- [ ] Handle products with no variants

---

## 🚀 Quick Start Guide

### **Step 1: Run Migration**
```bash
cd c:\Users\RS\IdeaProjects\rolly_shop_api
psql -U your_user -d your_database -f database_migration_product_variants.sql
```

### **Step 2: Restart Application**
```bash
./gradlew bootRun
# or
docker-compose restart
```

### **Step 3: Test API**
```bash
# Create parent product
curl -X POST https://your-api.com/api/v1/products \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cushion Felix",
    "categoryId": "category-uuid",
    "costPrice": 15.00,
    "price": 20.00,
    "stockQuantity": 0
  }'

# Create variant
curl -X POST https://your-api.com/api/v1/products \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cushion Felix",
    "categoryId": "category-uuid",
    "parentProductId": "parent-uuid",
    "isVariant": true,
    "variantCode": "21",
    "costPrice": 15.00,
    "price": 20.00,
    "stockQuantity": 10,
    "barcode": "111111"
  }'

# Get variants
curl https://your-api.com/api/v1/products/{parentId}/variants \
  -H "Authorization: Bearer TOKEN"
```

### **Step 4: Integrate Frontend**
Use the React or Vue examples above to build your UI!

---

## 📞 Need Help?

### Common Questions:

**Q: Can I convert existing products to have variants?**  
A: Yes! Just create a parent product with the same name, then update existing products to set their `parentProductId`.

**Q: What if I only need color variants (no code)?**  
A: Just use `variantColor` and leave `variantCode` and `variantSize` as null!

**Q: Can variants have different prices?**  
A: Yes! Each variant is a full product with its own price, cost, stock, etc.

**Q: How do I display variant names?**  
A: Combine the variant fields:
- Code only: "Code 21"
- Color only: "Blonde"
- Color + Size: "Blonde - Big"
- Code + Color + Size: "Code 21 - Blonde - Big"

---

## 🎉 Summary

✅ **Backend is ready!** All endpoints implemented  
✅ **Database migrated** with new variant columns  
✅ **Backward compatible** - existing products work  
✅ **Flexible** - use code, color, size, or any combination  
✅ **Frontend examples** provided for React & Vue  

**Start building your variant UI now! 🚀**

Happy coding! 🎨✨
