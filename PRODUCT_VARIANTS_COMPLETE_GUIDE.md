# 🎨 Product Variants - Complete Frontend Integration Guide

## 📋 Table of Contents
1. [Overview](#overview)
2. [Complete Workflow](#complete-workflow)
3. [API Endpoints](#api-endpoints)
4. [Request/Response Examples](#requestresponse-examples)
5. [React Implementation](#react-implementation)
6. [UI Examples](#ui-examples)

---

## 🎯 Overview

Your backend now supports the complete parent-variant workflow with:

✅ **Zero prices for parent products** - No need to enter prices when creating templates  
✅ **Auto-update parent prices** - First variant sets parent price automatically  
✅ **Total variant stock** - Parent shows combined stock from all variants  
✅ **Clean product listing** - Shows parent with variant count and total stock  
✅ **Detailed variant view** - Click parent to see all variants  

---

## 🔄 Complete Workflow

### Step 1: Create Parent Product (No Prices!)

**Frontend Form:**
```
┌─────────────────────────────────────┐
│ Create Product                      │
├─────────────────────────────────────┤
│ Name: Puma Shoe                     │
│ Description: Running shoes          │
│ Category: [Shoes ▼]                 │
│ Image: [Upload]                     │
│                                     │
│ ☑ Has variants (size/color/code)   │
│                                     │
│ ℹ️  You'll set prices when adding   │
│    variants                         │
│                                     │
│ [Create Parent Product]             │
└─────────────────────────────────────┘
```

**API Request:**
```json
POST /api/v1/products
{
  "name": "Puma Shoe",
  "description": "Running shoes",
  "categoryId": "uuid",
  "imageUrl": "https://...",
  "costPrice": 0,           // ✅ Now allowed for parents!
  "price": 0,               // ✅ Now allowed for parents!
  "stockQuantity": 0,       // Parent has no stock
  "discountPercent": 0
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "parent-uuid",
    "name": "Puma Shoe",
    "costPrice": 0,
    "price": 0,
    "stockQuantity": 0,
    "isVariant": false,
    "parentProductId": null,
    "variants": null
  }
}
```

---

### Step 2: Add First Variant

**Frontend Form:**
```
┌─────────────────────────────────────┐
│ Add Variant to: Puma Shoe           │
├─────────────────────────────────────┤
│ Variant Size: 42                    │
│ Cost Price: $50.00                  │
│ Selling Price: $100.00              │
│ Stock: 10                           │
│ Barcode: 123456                     │
│                                     │
│ [Add Variant]                       │
└─────────────────────────────────────┘
```

**API Request:**
```json
POST /api/v1/products
{
  "name": "Puma Shoe",
  "description": "Size 42",
  "parentProductId": "parent-uuid",
  "isVariant": true,
  "variantSize": "42",
  "costPrice": 50.00,
  "price": 100.00,
  "stockQuantity": 10,
  "barcode": "123456",
  "categoryId": "uuid"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "variant-uuid",
    "name": "Puma Shoe",
    "costPrice": 50.00,
    "price": 100.00,
    "stockQuantity": 10,
    "isVariant": true,
    "parentProductId": "parent-uuid",
    "variantSize": "42"
  }
}
```

**🎉 Parent price automatically updates to $100.00!**

---

### Step 3: View Products List

**API Request:**
```bash
GET /api/v1/products/admin/all?page=0&size=20
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "parent-uuid",
        "name": "Puma Shoe",
        "price": 100.00,
        "stockQuantity": 0,
        "hasVariants": true,
        "totalVariantStock": 10,      // ✅ Total across all variants!
        "isVariant": false,
        "parentProductId": null
      }
    ],
    "totalPages": 1,
    "totalElements": 1,
    "currentPage": 0
  }
}
```

**Display:**
```
📦 Puma Shoe
   Price: $100.00
   Stock: 10 (across variants)
   [View Details]
```

---

### Step 4: View Parent Details

**API Request:**
```bash
GET /api/v1/products/admin/{parent-uuid}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "parent-uuid",
    "name": "Puma Shoe",
    "price": 100.00,
    "stockQuantity": 0,
    "totalVariantStock": 10,
    "hasVariants": true,
    "variants": [
      {
        "id": "variant-1-uuid",
        "variantSize": "42",
        "stockQuantity": 10,
        "price": 100.00,
        "barcode": "123456"
      }
    ]
  }
}
```

**Display:**
```
📦 Puma Shoe (Parent Product)
   Price: $100.00
   Total Stock: 10 units

   Variants:
   ┌──────┬───────┬────────┬──────────┐
   │ Size │ Stock │ Price  │ Barcode  │
   ├──────┼───────┼────────┼──────────┤
   │ 42   │ 10    │ $100   │ 123456   │
   └──────┴───────┴────────┴──────────┘

   [Add More Variants]
```

---

## 🔌 API Endpoints Reference

### 1. **Create Parent Product**

```
POST /api/v1/products
```

**Request:**
```json
{
  "name": "Product Name",
  "description": "Description",
  "categoryId": "uuid",
  "imageUrl": "https://...",
  "costPrice": 0,           // ✅ Zero allowed for parents
  "price": 0,               // ✅ Zero allowed for parents
  "stockQuantity": 0,
  "discountPercent": 0
}
```

**Response:** Product object with `isVariant: false`

---

### 2. **Create Variant**

```
POST /api/v1/products
```

**Request:**
```json
{
  "name": "Product Name",
  "parentProductId": "parent-uuid",
  "isVariant": true,
  "variantCode": "21",        // Optional
  "variantColor": "Blonde",   // Optional
  "variantSize": "42",        // Optional
  "costPrice": 50.00,         // Required
  "price": 100.00,            // Required
  "stockQuantity": 10,        // Required
  "barcode": "123456",
  "categoryId": "uuid"
}
```

**Response:** Product object with `isVariant: true`

**🎉 First variant automatically updates parent price!**

---

### 3. **Get All Products (Admin)**

```
GET /api/v1/products/admin/all?page=0&size=20
```

**Query Parameters:**
- `page` - Page number (default: 0)
- `size` - Items per page (default: 20)
- `categoryId` - Filter by category (optional)
- `search` - Search by name or barcode (optional)
- `sortBy` - Sort field (default: createdAt)
- `direction` - Sort direction: asc/desc (default: desc)

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "Product Name",
        "price": 100.00,
        "stockQuantity": 0,
        "hasVariants": true,            // ✅ Has variants
        "totalVariantStock": 23,        // ✅ Total across variants
        "isVariant": false,
        "parentProductId": null
      }
    ],
    "totalPages": 5,
    "totalElements": 100,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

---

### 4. **Get Product Details (Admin)**

```
GET /api/v1/products/admin/{productId}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "Product Name",
    "description": "Description",
    "price": 100.00,
    "stockQuantity": 0,
    "isVariant": false,
    "parentProductId": null,
    "hasVariants": true,
    "totalVariantStock": 23,           // ✅ Total stock
    "variants": [                      // ✅ All variants
      {
        "id": "variant-uuid",
        "variantCode": "21",
        "variantColor": null,
        "variantSize": null,
        "stockQuantity": 10,
        "price": 100.00,
        "barcode": "123456"
      }
    ]
  }
}
```

---

### 5. **Get Product Variants**

```
GET /api/v1/products/{parentId}/variants
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "variant-uuid",
      "variantCode": "21",
      "variantColor": null,
      "variantSize": null,
      "stockQuantity": 10,
      "price": 100.00,
      "costPrice": 50.00,
      "profit": 50.00,
      "barcode": "123456"
    }
  ]
}
```

---

### 6. **Update Product**

```
PUT /api/v1/products/{productId}
```

**Request:** Same as create (all fields)

**Response:** Updated product object

---

### 7. **Delete Product**

```
DELETE /api/v1/products/{productId}
```

**Note:** Cannot delete parent if it has variants!

---

## 💻 React Implementation

### Complete Product Management Component

```tsx
import { useState, useEffect } from 'react';

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  costPrice: number;
  stockQuantity: number;
  hasVariants: boolean;
  totalVariantStock?: number;
  isVariant: boolean;
  parentProductId?: string;
  variantCode?: string;
  variantColor?: string;
  variantSize?: string;
  categoryId: string;
  imageUrl?: string;
  barcode?: string;
}

interface Variant {
  id: string;
  variantCode?: string;
  variantColor?: string;
  variantSize?: string;
  stockQuantity: number;
  price: number;
  barcode?: string;
}

// ==================== CREATE PARENT PRODUCT ====================

const CreateParentProductForm = ({ onSuccess }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    categoryId: '',
    imageUrl: '',
    hasVariants: false
  });

  const handleSubmit = async (e) => {
    e.preventDefault();

    const request = {
      name: formData.name,
      description: formData.description,
      categoryId: formData.categoryId,
      imageUrl: formData.imageUrl,
      costPrice: 0,          // ✅ Zero allowed for parents!
      price: 0,              // ✅ Zero allowed for parents!
      stockQuantity: 0,
      discountPercent: 0
    };

    try {
      const response = await fetch('/api/v1/products', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(request)
      });

      const result = await response.json();
      
      if (result.success) {
        alert('Parent product created! Now add variants.');
        onSuccess(result.data);
      } else {
        alert(`Error: ${result.message}`);
      }
    } catch (error) {
      console.error('Create failed:', error);
      alert('Failed to create product');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block font-medium">Product Name *</label>
        <input
          type="text"
          value={formData.name}
          onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          required
          className="w-full border rounded px-3 py-2"
        />
      </div>

      <div>
        <label className="block font-medium">Description</label>
        <textarea
          value={formData.description}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          className="w-full border rounded px-3 py-2"
        />
      </div>

      <div>
        <label className="block font-medium">Category *</label>
        <select
          value={formData.categoryId}
          onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
          required
          className="w-full border rounded px-3 py-2"
        >
          <option value="">Select category...</option>
          {/* Load categories here */}
        </select>
      </div>

      <div>
        <label className="block font-medium">Product Image</label>
        <input
          type="text"
          value={formData.imageUrl}
          onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
          placeholder="https://..."
          className="w-full border rounded px-3 py-2"
        />
      </div>

      <div className="bg-blue-50 border border-blue-200 rounded p-4">
        <p className="text-sm text-blue-800">
          ℹ️  You'll set prices and stock when adding variants
        </p>
      </div>

      <button
        type="submit"
        className="bg-blue-500 text-white px-6 py-2 rounded hover:bg-blue-600"
      >
        Create Parent Product
      </button>
    </form>
  );
};

// ==================== ADD VARIANT ====================

const AddVariantForm = ({ parentProduct, onSuccess }) => {
  const [formData, setFormData] = useState({
    variantCode: '',
    variantColor: '',
    variantSize: '',
    costPrice: '',
    price: '',
    stockQuantity: '',
    barcode: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();

    const request = {
      name: parentProduct.name,
      description: formData.variantSize ? `Size ${formData.variantSize}` : '',
      parentProductId: parentProduct.id,
      isVariant: true,
      variantCode: formData.variantCode || undefined,
      variantColor: formData.variantColor || undefined,
      variantSize: formData.variantSize || undefined,
      costPrice: parseFloat(formData.costPrice),
      price: parseFloat(formData.price),
      stockQuantity: parseInt(formData.stockQuantity),
      barcode: formData.barcode || undefined,
      categoryId: parentProduct.categoryId,
      discountPercent: 0
    };

    try {
      const response = await fetch('/api/v1/products', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(request)
      });

      const result = await response.json();
      
      if (result.success) {
        alert('Variant added!');
        onSuccess(result.data);
        setFormData({
          variantCode: '',
          variantColor: '',
          variantSize: '',
          costPrice: '',
          price: '',
          stockQuantity: '',
          barcode: ''
        });
      } else {
        alert(`Error: ${result.message}`);
      }
    } catch (error) {
      console.error('Add variant failed:', error);
      alert('Failed to add variant');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <h3 className="text-lg font-bold">
        Add Variant to: {parentProduct.name}
      </h3>

      <div className="grid grid-cols-3 gap-4">
        <div>
          <label className="block font-medium">Variant Code</label>
          <input
            type="text"
            value={formData.variantCode}
            onChange={(e) => setFormData({ ...formData, variantCode: e.target.value })}
            placeholder="e.g., 21, 23"
            className="w-full border rounded px-3 py-2"
          />
        </div>

        <div>
          <label className="block font-medium">Variant Color</label>
          <input
            type="text"
            value={formData.variantColor}
            onChange={(e) => setFormData({ ...formData, variantColor: e.target.value })}
            placeholder="e.g., Blonde"
            className="w-full border rounded px-3 py-2"
          />
        </div>

        <div>
          <label className="block font-medium">Variant Size</label>
          <input
            type="text"
            value={formData.variantSize}
            onChange={(e) => setFormData({ ...formData, variantSize: e.target.value })}
            placeholder="e.g., 42, Large"
            className="w-full border rounded px-3 py-2"
          />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block font-medium">Cost Price *</label>
          <input
            type="number"
            step="0.01"
            value={formData.costPrice}
            onChange={(e) => setFormData({ ...formData, costPrice: e.target.value })}
            required
            min="0.01"
            className="w-full border rounded px-3 py-2"
          />
        </div>

        <div>
          <label className="block font-medium">Selling Price *</label>
          <input
            type="number"
            step="0.01"
            value={formData.price}
            onChange={(e) => setFormData({ ...formData, price: e.target.value })}
            required
            min="0.01"
            className="w-full border rounded px-3 py-2"
          />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block font-medium">Stock Quantity *</label>
          <input
            type="number"
            value={formData.stockQuantity}
            onChange={(e) => setFormData({ ...formData, stockQuantity: e.target.value })}
            required
            min="0"
            className="w-full border rounded px-3 py-2"
          />
        </div>

        <div>
          <label className="block font-medium">Barcode</label>
          <input
            type="text"
            value={formData.barcode}
            onChange={(e) => setFormData({ ...formData, barcode: e.target.value })}
            className="w-full border rounded px-3 py-2"
          />
        </div>
      </div>

      <button
        type="submit"
        className="bg-green-500 text-white px-6 py-2 rounded hover:bg-green-600"
      >
        Add Variant
      </button>
    </form>
  );
};

// ==================== PRODUCTS LIST ====================

const ProductsList = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await fetch('/api/v1/products/admin/all?page=0&size=100', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      const result = await response.json();
      
      if (result.success) {
        setProducts(result.data.content);
      }
    } catch (error) {
      console.error('Fetch failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStockDisplay = (product: Product) => {
    if (product.hasVariants && product.totalVariantStock !== undefined) {
      return `${product.totalVariantStock} (across variants)`;
    }
    return product.stockQuantity.toString();
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold">Products</h2>

      <table className="w-full border">
        <thead className="bg-gray-100">
          <tr>
            <th className="p-2 text-left">Name</th>
            <th className="p-2 text-left">Price</th>
            <th className="p-2 text-left">Stock</th>
            <th className="p-2 text-left">Type</th>
            <th className="p-2 text-left">Actions</th>
          </tr>
        </thead>
        <tbody>
          {products.map((product) => (
            <tr key={product.id} className="border-t">
              <td className="p-2">
                {product.hasVariants && '📦 '}
                {product.name}
                {product.hasVariants && (
                  <span className="text-sm text-gray-500 ml-2">
                    ({product.totalVariantStock} units across variants)
                  </span>
                )}
              </td>
              <td className="p-2">${product.price.toFixed(2)}</td>
              <td className="p-2">{getStockDisplay(product)}</td>
              <td className="p-2">
                {product.hasVariants ? (
                  <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded text-sm">
                    Parent
                  </span>
                ) : product.isVariant ? (
                  <span className="bg-green-100 text-green-800 px-2 py-1 rounded text-sm">
                    Variant
                  </span>
                ) : (
                  <span className="bg-gray-100 text-gray-800 px-2 py-1 rounded text-sm">
                    Product
                  </span>
                )}
              </td>
              <td className="p-2">
                <button
                  onClick={() => window.location.href = `/products/${product.id}`}
                  className="text-blue-500 hover:underline"
                >
                  View Details
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

// ==================== PRODUCT DETAILS ====================

const ProductDetails = ({ productId }) => {
  const [product, setProduct] = useState<Product | null>(null);
  const [variants, setVariants] = useState<Variant[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProductDetails();
  }, [productId]);

  const fetchProductDetails = async () => {
    try {
      const response = await fetch(`/api/v1/products/admin/${productId}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      const result = await response.json();
      
      if (result.success) {
        setProduct(result.data);
        if (result.data.variants) {
          setVariants(result.data.variants);
        }
      }
    } catch (error) {
      console.error('Fetch failed:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (!product) return <div>Product not found</div>;

  return (
    <div className="space-y-6">
      <div className="border rounded p-6">
        <h1 className="text-3xl font-bold mb-4">
          {product.hasVariants && '📦 '}
          {product.name}
        </h1>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-gray-600">Description</p>
            <p className="font-medium">{product.description || 'N/A'}</p>
          </div>

          <div>
            <p className="text-gray-600">Price</p>
            <p className="font-medium">${product.price.toFixed(2)}</p>
          </div>

          {product.hasVariants ? (
            <div>
              <p className="text-gray-600">Total Variant Stock</p>
              <p className="font-medium text-2xl">{product.totalVariantStock}</p>
            </div>
          ) : (
            <div>
              <p className="text-gray-600">Stock</p>
              <p className="font-medium">{product.stockQuantity}</p>
            </div>
          )}
        </div>
      </div>

      {product.hasVariants && variants.length > 0 && (
        <div className="border rounded p-6">
          <h2 className="text-2xl font-bold mb-4">Variants</h2>

          <table className="w-full">
            <thead className="bg-gray-100">
              <tr>
                <th className="p-2 text-left">Code</th>
                <th className="p-2 text-left">Color</th>
                <th className="p-2 text-left">Size</th>
                <th className="p-2 text-left">Stock</th>
                <th className="p-2 text-left">Price</th>
                <th className="p-2 text-left">Barcode</th>
              </tr>
            </thead>
            <tbody>
              {variants.map((variant) => (
                <tr key={variant.id} className="border-t">
                  <td className="p-2">{variant.variantCode || '-'}</td>
                  <td className="p-2">{variant.variantColor || '-'}</td>
                  <td className="p-2">{variant.variantSize || '-'}</td>
                  <td className="p-2">{variant.stockQuantity}</td>
                  <td className="p-2">${variant.price.toFixed(2)}</td>
                  <td className="p-2">{variant.barcode || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="mt-4">
            <button className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600">
              Add More Variants
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export { CreateParentProductForm, AddVariantForm, ProductsList, ProductDetails };
```

---

## ✅ Summary

### What You Get:

1. ✅ **Easy parent creation** - No price input needed
2. ✅ **Auto-price update** - First variant sets parent price
3. ✅ **Total variant stock** - See combined stock in list view
4. ✅ **Detailed variant view** - Click to see all variants
5. ✅ **Clean workflow** - Intuitive UI flow

### API Endpoints to Use:

| Action | Endpoint | Method |
|--------|----------|--------|
| Create parent | `/api/v1/products` | POST |
| Create variant | `/api/v1/products` | POST |
| List products | `/api/v1/products/admin/all` | GET |
| Get details | `/api/v1/products/admin/{id}` | GET |
| Get variants | `/api/v1/products/{id}/variants` | GET |

---

**Your backend is ready! Use the React code above to build your UI! 🚀**
