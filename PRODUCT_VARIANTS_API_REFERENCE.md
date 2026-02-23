# 🚀 Product Variants API - Quick Reference

## ✅ Backend Changes Complete!

Your backend now supports:
- ✅ Zero prices for parent products
- ✅ Auto-update parent prices from first variant
- ✅ Total variant stock calculation
- ✅ Enhanced product listing with variant info

---

## 📋 Quick API Reference

### 1️⃣ Create Parent Product (No Prices!)

```bash
POST /api/v1/products
```

```json
{
  "name": "Puma Shoe",
  "description": "Running shoes",
  "categoryId": "uuid",
  "imageUrl": "https://...",
  "costPrice": 0,        // ✅ Zero allowed!
  "price": 0,            // ✅ Zero allowed!
  "stockQuantity": 0,
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
    "price": 0,
    "stockQuantity": 0
  }
}
```

---

### 2️⃣ Add Variant

```bash
POST /api/v1/products
```

```json
{
  "name": "Puma Shoe",
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
    "variantSize": "42",
    "price": 100.00,
    "stockQuantity": 10
  }
}
```

🎉 **Parent price automatically updates to $100!**

---

### 3️⃣ List All Products

```bash
GET /api/v1/products/admin/all?page=0&size=20
```

**Query Params:**
- `page` - Page number (default: 0)
- `size` - Per page (default: 20)
- `categoryId` - Filter by category
- `search` - Search name/barcode
- `sortBy` - Sort field
- `direction` - asc/desc

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "Puma Shoe",
        "price": 100.00,
        "stockQuantity": 0,
        "hasVariants": true,
        "totalVariantStock": 10,  // ✅ Total!
        "isVariant": false
      }
    ]
  }
}
```

---

### 4️⃣ Get Product Details

```bash
GET /api/v1/products/admin/{productId}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "Puma Shoe",
    "price": 100.00,
    "totalVariantStock": 10,
    "variants": [
      {
        "id": "variant-uuid",
        "variantSize": "42",
        "stockQuantity": 10,
        "price": 100.00,
        "barcode": "123456"
      }
    ]
  }
}
```

---

### 5️⃣ Get Variants Only

```bash
GET /api/v1/products/{parentId}/variants
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "variant-uuid",
      "variantSize": "42",
      "stockQuantity": 10,
      "price": 100.00
    }
  ]
}
```

---

## 🎨 Frontend Flow

### Step 1: Create Parent
```javascript
const createParent = async (formData) => {
  const response = await fetch('/api/v1/products', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      name: formData.name,
      categoryId: formData.categoryId,
      costPrice: 0,           // ✅ No price needed!
      price: 0,
      stockQuantity: 0
    })
  });
  
  return response.json();
};
```

### Step 2: Add Variant
```javascript
const addVariant = async (parentId, variantData) => {
  const response = await fetch('/api/v1/products', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      name: variantData.name,
      parentProductId: parentId,
      isVariant: true,
      variantSize: variantData.size,
      costPrice: variantData.costPrice,
      price: variantData.price,
      stockQuantity: variantData.stock,
      categoryId: variantData.categoryId
    })
  });
  
  return response.json();
};
```

### Step 3: Display Products
```javascript
const ProductRow = ({ product }) => {
  const stockDisplay = product.hasVariants 
    ? `${product.totalVariantStock} (across variants)`
    : product.stockQuantity;

  return (
    <tr>
      <td>{product.hasVariants ? '📦 ' : ''}{product.name}</td>
      <td>${product.price}</td>
      <td>{stockDisplay}</td>
      <td>
        {product.hasVariants && (
          <button onClick={() => viewDetails(product.id)}>
            View Variants
          </button>
        )}
      </td>
    </tr>
  );
};
```

---

## 🎯 Key Points

### Parent Products:
- ✅ `costPrice: 0` allowed
- ✅ `price: 0` allowed
- ✅ `stockQuantity: 0` required
- ✅ First variant updates parent price

### Variant Products:
- ✅ `parentProductId` required
- ✅ `isVariant: true` required
- ✅ Price & stock required (> 0)
- ✅ Use `variantCode`, `variantColor`, or `variantSize`

### Product List:
- ✅ Shows `hasVariants: true` for parents
- ✅ Shows `totalVariantStock` for parents
- ✅ Parents have `stockQuantity: 0`

### Product Details:
- ✅ Shows `variants` array for parents
- ✅ Shows `totalVariantStock` for parents
- ✅ Variants show individual stock

---

## 📝 Response Fields

### ProductAdminSimpleResponse (List):
```typescript
{
  id: string;
  name: string;
  price: number;
  stockQuantity: number;
  hasVariants: boolean;         // ✅ Has variants?
  totalVariantStock?: number;   // ✅ Total stock
  isVariant: boolean;
  parentProductId?: string;
  variantCode?: string;
  variantColor?: string;
  variantSize?: string;
}
```

### ProductAdminResponse (Details):
```typescript
{
  id: string;
  name: string;
  price: number;
  stockQuantity: number;
  hasVariants: boolean;
  totalVariantStock?: number;
  variants?: [                  // ✅ All variants
    {
      id: string;
      variantCode?: string;
      variantColor?: string;
      variantSize?: string;
      stockQuantity: number;
      price: number;
      barcode?: string;
    }
  ];
}
```

---

## 🧪 Testing

### Test 1: Create Parent (No Prices)
```bash
curl -X POST https://api/products \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "name": "Test Product",
    "categoryId": "uuid",
    "costPrice": 0,
    "price": 0,
    "stockQuantity": 0
  }'
```

✅ Should succeed!

### Test 2: Add Variant
```bash
curl -X POST https://api/products \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "name": "Test Product",
    "parentProductId": "parent-uuid",
    "isVariant": true,
    "variantSize": "L",
    "costPrice": 10,
    "price": 20,
    "stockQuantity": 5,
    "categoryId": "uuid"
  }'
```

✅ Parent price updates to $20!

### Test 3: List Products
```bash
curl https://api/products/admin/all?page=0
```

✅ Shows `totalVariantStock: 5`!

---

## 🎉 You're Ready!

1. ✅ Backend updated
2. ✅ Zero prices allowed for parents
3. ✅ Auto-price update working
4. ✅ Total stock calculation ready
5. ✅ API documented

**Use the React code in `PRODUCT_VARIANTS_COMPLETE_GUIDE.md` to build your UI!**

---

**Happy coding! 🚀**
