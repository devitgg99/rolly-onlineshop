# 🎯 Admin Products Endpoint - Filter & Search Guide

## ✅ Updated! Now with Category Filter & Search

The admin products endpoint has been enhanced to support:
- ✅ Filter by category
- ✅ Search by product name or barcode
- ✅ Combine both filters
- ✅ All existing features (pagination, sorting)

---

## 📍 Endpoint

```
GET /api/v1/products/admin/all
```

**Authentication Required:** Admin role only 🔒

---

## 📋 Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | Integer | No | 0 | Page number (0-based) |
| `size` | Integer | No | 20 | Items per page |
| `categoryId` | UUID | No | null | Filter by category ID |
| `search` | String | No | null | Search by product name or barcode |
| `sortBy` | String | No | createdAt | Sort field (name, price, stockQuantity, etc.) |
| `direction` | String | No | desc | Sort direction (asc or desc) |

---

## 📝 Example Requests

### 1. Get All Products (No Filter)
```bash
GET /api/v1/products/admin/all?page=0&size=20
```

**Use case:** Default product list

---

### 2. Filter by Category
```bash
GET /api/v1/products/admin/all?page=0&size=20&categoryId=123e4567-e89b-12d3-a456-426614174000
```

**Use case:** Show only products in "Electronics" category

---

### 3. Search Products
```bash
GET /api/v1/products/admin/all?page=0&size=20&search=phone
```

**Searches in:**
- ✅ Product name (case-insensitive)
- ✅ Barcode

**Use case:** User types "phone" in search box

---

### 4. Combined: Category + Search
```bash
GET /api/v1/products/admin/all?page=0&size=20&categoryId=123e4567-e89b-12d3-a456-426614174000&search=samsung
```

**Use case:** Show Samsung products in Electronics category

---

### 5. Search by Barcode
```bash
GET /api/v1/products/admin/all?page=0&size=20&search=8801234567890
```

**Use case:** Scan barcode and find product

---

### 6. With Sorting
```bash
GET /api/v1/products/admin/all?page=0&size=20&categoryId=123e4567&sortBy=price&direction=asc
```

**Use case:** Show products in category sorted by price (low to high)

---

## 📊 Response Format

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "Samsung Galaxy S23",
        "barcode": "8801234567890",
        "costPrice": 500.00,
        "price": 800.00,
        "discountPercent": 10,
        "discountedPrice": 720.00,
        "profit": 220.00,
        "stockQuantity": 25,
        "imageUrl": "https://...",
        "brandName": null,
        "categoryName": "Electronics"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false
  },
  "message": "Products retrieved (admin)"
}
```

---

## 💻 Frontend Integration Examples

### React/TypeScript Example

```typescript
import { useState, useEffect } from 'react';

interface ProductFilters {
  page: number;
  size: number;
  categoryId?: string;
  search?: string;
  sortBy?: string;
  direction?: 'asc' | 'desc';
}

function ProductsPage() {
  const [filters, setFilters] = useState<ProductFilters>({
    page: 0,
    size: 20,
    sortBy: 'createdAt',
    direction: 'desc'
  });
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchProducts();
  }, [filters]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      // Build query string
      const params = new URLSearchParams();
      params.append('page', filters.page.toString());
      params.append('size', filters.size.toString());
      
      if (filters.categoryId) {
        params.append('categoryId', filters.categoryId);
      }
      
      if (filters.search) {
        params.append('search', filters.search);
      }
      
      params.append('sortBy', filters.sortBy || 'createdAt');
      params.append('direction', filters.direction || 'desc');

      const response = await fetch(
        `/api/v1/products/admin/all?${params.toString()}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );
      
      const data = await response.json();
      setProducts(data.data.content);
    } catch (error) {
      console.error('Error fetching products:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {/* Category Filter */}
      <select 
        value={filters.categoryId || ''}
        onChange={(e) => setFilters({
          ...filters, 
          categoryId: e.target.value || undefined,
          page: 0 // Reset to first page
        })}
      >
        <option value="">All Categories</option>
        <option value="category-uuid-1">Electronics</option>
        <option value="category-uuid-2">Clothing</option>
      </select>

      {/* Search Box */}
      <input
        type="text"
        placeholder="Search by name or barcode..."
        value={filters.search || ''}
        onChange={(e) => setFilters({
          ...filters,
          search: e.target.value || undefined,
          page: 0 // Reset to first page
        })}
      />

      {/* Products List */}
      {loading ? (
        <div>Loading...</div>
      ) : (
        <div>
          {products.map(product => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </div>
  );
}
```

---

### Vue.js Example

```vue
<template>
  <div>
    <!-- Category Filter -->
    <select v-model="filters.categoryId" @change="resetPage">
      <option :value="null">All Categories</option>
      <option v-for="category in categories" :key="category.id" :value="category.id">
        {{ category.name }}
      </option>
    </select>

    <!-- Search Box -->
    <input
      v-model="filters.search"
      @input="resetPage"
      placeholder="Search by name or barcode..."
    />

    <!-- Products Grid -->
    <div v-if="loading">Loading...</div>
    <div v-else class="products-grid">
      <ProductCard
        v-for="product in products"
        :key="product.id"
        :product="product"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';

const filters = reactive({
  page: 0,
  size: 20,
  categoryId: null as string | null,
  search: null as string | null,
  sortBy: 'createdAt',
  direction: 'desc'
});

const products = ref([]);
const loading = ref(false);

watch(filters, () => {
  fetchProducts();
}, { deep: true });

const fetchProducts = async () => {
  loading.value = true;
  try {
    const params = new URLSearchParams();
    params.append('page', filters.page.toString());
    params.append('size', filters.size.toString());
    
    if (filters.categoryId) params.append('categoryId', filters.categoryId);
    if (filters.search) params.append('search', filters.search);
    
    params.append('sortBy', filters.sortBy);
    params.append('direction', filters.direction);

    const response = await fetch(`/api/v1/products/admin/all?${params}`);
    const data = await response.json();
    products.value = data.data.content;
  } catch (error) {
    console.error('Error:', error);
  } finally {
    loading.value = false;
  }
};

const resetPage = () => {
  filters.page = 0;
};
</script>
```

---

### JavaScript (Vanilla) Example

```javascript
class ProductManager {
  constructor() {
    this.filters = {
      page: 0,
      size: 20,
      categoryId: null,
      search: null,
      sortBy: 'createdAt',
      direction: 'desc'
    };
  }

  async fetchProducts() {
    const params = new URLSearchParams();
    params.append('page', this.filters.page);
    params.append('size', this.filters.size);
    
    if (this.filters.categoryId) {
      params.append('categoryId', this.filters.categoryId);
    }
    
    if (this.filters.search) {
      params.append('search', this.filters.search);
    }
    
    params.append('sortBy', this.filters.sortBy);
    params.append('direction', this.filters.direction);

    try {
      const response = await fetch(
        `/api/v1/products/admin/all?${params.toString()}`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );
      
      const data = await response.json();
      this.renderProducts(data.data.content);
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  }

  setCategory(categoryId) {
    this.filters.categoryId = categoryId;
    this.filters.page = 0; // Reset to first page
    this.fetchProducts();
  }

  setSearch(searchTerm) {
    this.filters.search = searchTerm;
    this.filters.page = 0; // Reset to first page
    this.fetchProducts();
  }

  renderProducts(products) {
    // Your rendering logic here
  }
}

// Usage
const productManager = new ProductManager();

// Category filter
document.getElementById('categoryFilter').addEventListener('change', (e) => {
  productManager.setCategory(e.target.value || null);
});

// Search box
document.getElementById('searchBox').addEventListener('input', (e) => {
  productManager.setSearch(e.target.value || null);
});
```

---

## 🎯 Common Use Cases

### 1. Product Management Dashboard
```javascript
// Show all products
fetch('/api/v1/products/admin/all?page=0&size=20')
```

### 2. Category-Specific Inventory
```javascript
// Show only electronics
fetch('/api/v1/products/admin/all?categoryId=electronics-uuid&page=0&size=20')
```

### 3. Quick Search
```javascript
// User types in search box
fetch('/api/v1/products/admin/all?search=iphone&page=0&size=20')
```

### 4. Barcode Scanner
```javascript
// Scan barcode to find product
fetch('/api/v1/products/admin/all?search=8801234567890')
```

### 5. Filtered Inventory Report
```javascript
// Electronics sorted by stock (low to high)
fetch('/api/v1/products/admin/all?categoryId=uuid&sortBy=stockQuantity&direction=asc')
```

---

## ⚡ Performance Tips

### 1. Debounce Search Input
```javascript
let searchTimeout;
const handleSearch = (value) => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    fetchProducts({ search: value });
  }, 300); // Wait 300ms after user stops typing
};
```

### 2. Clear Empty Filters
```javascript
// Don't send null/empty parameters
const params = new URLSearchParams();
if (categoryId) params.append('categoryId', categoryId);
if (search && search.trim()) params.append('search', search);
```

### 3. Cache Category List
```javascript
// Fetch categories once and cache
const categories = await fetchCategories();
localStorage.setItem('categories', JSON.stringify(categories));
```

---

## 🔄 Migration Guide

### If You Have Existing Code:

**Before:**
```javascript
fetch('/api/v1/products/admin/all?page=0&size=20')
```

**After (with filters):**
```javascript
// Still works! No breaking changes
fetch('/api/v1/products/admin/all?page=0&size=20')

// Now you can add filters
fetch('/api/v1/products/admin/all?page=0&size=20&categoryId=uuid&search=phone')
```

✅ **No breaking changes!** Your existing code continues to work.

---

## ✅ Testing Checklist

- [ ] Get all products (no filters)
- [ ] Filter by category
- [ ] Search by product name
- [ ] Search by barcode
- [ ] Combine category + search
- [ ] Test pagination with filters
- [ ] Test sorting with filters
- [ ] Verify empty results when no matches
- [ ] Test special characters in search
- [ ] Test very long search terms

---

## 🎉 Summary

**What Changed:**
- ✅ Added `categoryId` parameter (optional)
- ✅ Added `search` parameter (optional)
- ✅ Search works on name AND barcode
- ✅ Can combine both filters
- ✅ No breaking changes to existing API

**What You Can Do Now:**
- ✅ Filter products by category
- ✅ Search products by name or barcode
- ✅ Build powerful product management UI
- ✅ Faster product lookup
- ✅ Better inventory management

**Ready to Use!** 🚀

Start integrating and enjoy the enhanced filtering! 😊
