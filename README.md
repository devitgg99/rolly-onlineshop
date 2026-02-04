# Rolly Shop API

A comprehensive e-commerce REST API built with **Spring Boot 4** and **Kotlin**.

## 🚀 Tech Stack

- **Framework:** Spring Boot 4.0.1
- **Language:** Kotlin 2.2.21
- **Database:** PostgreSQL
- **Authentication:** JWT (JSON Web Tokens)
- **Storage:** AWS S3
- **Documentation:** Swagger/OpenAPI 3

---

## 📋 API Endpoints

### Base URL
```
http://localhost:8080/api/v1
```

### 📖 API Documentation (Swagger)
```
http://localhost:8080/swagger-ui.html
```

---

## 🔓 Authentication Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/auth/register` | Register new user | 🌐 Public |
| POST | `/auth/login` | Login and get JWT token | 🌐 Public |

### Register Request
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "0123456789",
  "password": "password123"
}
```

### Login Request
```json
{
  "emailOrPhonenumber": "john@example.com",
  "password": "password123"
}
```

### Login Response
```json
{
  "success": true,
  "message": "Login successful",
  "data": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## 🖼️ Image Processing Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/images/remove-background` | Remove background from image & upload to S3 | 🌐 Public |

### Request
- **Content-Type:** `multipart/form-data`
- **Parameters:**
  - `image` (file, required): Image file to process
  - `fileName` (string, optional): Custom filename for S3

### Response
```json
{
  "success": true,
  "message": "Background removed and saved to S3",
  "data": {
    "url": "https://bucket.s3.region.amazonaws.com/images/filename.png"
  }
}
```

---

## 🏷️ Brand Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/brands` | Get all brands | 🌐 Public |
| GET | `/brands/{id}` | Get brand by ID | 🌐 Public |
| POST | `/brands` | Create new brand | 🔒 Admin |
| PUT | `/brands/{id}` | Update brand | 🔒 Admin |
| DELETE | `/brands/{id}` | Delete brand | 🔒 Admin |

### Brand Request Body
```json
{
  "name": "Nike",
  "logoUrl": "https://example.com/nike-logo.png",
  "description": "Just Do It"
}
```

---

## 📂 Category Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/categories` | Get all categories | 🌐 Public |
| GET | `/categories/{id}` | Get category by ID | 🌐 Public |
| GET | `/categories/root` | Get root (top-level) categories | 🌐 Public |
| GET | `/categories/{parentId}/subcategories` | Get subcategories | 🌐 Public |
| POST | `/categories` | Create new category | 🔒 Admin |
| PUT | `/categories/{id}` | Update category | 🔒 Admin |
| DELETE | `/categories/{id}` | Delete category | 🔒 Admin |

### Category Request Body
```json
{
  "name": "Electronics",
  "description": "Electronic devices and gadgets",
  "imageUrl": "https://example.com/electronics.png",
  "parentId": null
}
```

> 💡 **Subcategories:** Set `parentId` to create nested categories (e.g., Electronics → Phones → iPhone)

---

## 📦 Product Endpoints

### Public Endpoints
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/products` | Get all products (paginated) | 🌐 Public |
| GET | `/products/{id}` | Get product details | 🌐 Public |
| GET | `/products/brand/{brandId}` | Get products by brand | 🌐 Public |
| GET | `/products/category/{categoryId}` | Get products by category | 🌐 Public |
| GET | `/products/search?q={query}` | Search products by name | 🌐 Public |

### Admin Endpoints
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/products/admin/inventory` | **Inventory table with sales data** | 🔒 Admin |
| GET | `/products/admin/all` | Get all products (with cost price) | 🔒 Admin |
| GET | `/products/admin/{id}` | Get product details (with cost) | 🔒 Admin |
| GET | `/products/admin/stats` | Get inventory statistics | 🔒 Admin |
| GET | `/products/admin/low-stock?threshold=10` | Get low stock products | 🔒 Admin |
| GET | `/products/barcode/{barcode}` | Find product by barcode | 🔒 Admin |
| POST | `/products` | Create new product | 🔒 Admin |
| PUT | `/products/{id}` | Update product | 🔒 Admin |
| DELETE | `/products/{id}` | Delete product | 🔒 Admin |

### Query Parameters (GET /products)
| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | 0 | Page number (0-based) |
| `size` | 10 | Items per page |
| `sortBy` | createdAt | Sort field |
| `direction` | desc | Sort direction (asc/desc) |

### Product Request Body
```json
{
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 chip",
  "price": 999.99,
  "discountPercent": 10,
  "stockQuantity": 100,
  "imageUrl": "https://example.com/iphone.png",
  "brandId": "uuid-of-brand",
  "categoryId": "uuid-of-category"
}
```

### Product Response
```json
{
  "id": "uuid",
  "name": "iPhone 15 Pro",
  "price": 999.99,
  "discountPercent": 10,
  "discountedPrice": 899.99,
  "stockQuantity": 100,
  "brand": { "id": "...", "name": "Apple" },
  "category": { "id": "...", "name": "Phones" },
  "averageRating": 4.5
}
```

### Inventory Table Response (Admin)
`GET /products/admin/inventory` - Shows all product data with sales info:
```json
{
  "id": "uuid",
  "name": "iPhone 15 Pro",
  "barcode": "123456789",
  "categoryName": "Phones",
  "brandName": "Apple",
  "costPrice": 800.00,
  "price": 999.99,
  "discountPercent": 10,
  "sellingPrice": 899.99,
  "profit": 99.99,
  "stockQuantity": 45,
  "stockValue": 36000.00,
  "totalSold": 150,
  "totalRevenue": 134998.50,
  "totalProfit": 14998.50,
  "imageUrl": "https://...",
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-01-20T00:00:00Z"
}
```

---

## 📍 Address Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/addresses` | Get my addresses | 👤 User |
| GET | `/addresses/{id}` | Get address by ID | 👤 User |
| POST | `/addresses` | Add new address | 👤 User |
| PUT | `/addresses/{id}` | Update address | 👤 User |
| DELETE | `/addresses/{id}` | Delete address | 👤 User |
| PATCH | `/addresses/{id}/default` | Set as default address | 👤 User |

### Address Request Body
```json
{
  "fullName": "John Doe",
  "phoneNumber": "0123456789",
  "addressLine": "123 Main Street",
  "city": "Phnom Penh",
  "province": "Phnom Penh",
  "postalCode": "12000",
  "isDefault": true
}
```

---

## 🛒 Shopping Cart Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/cart` | Get my cart | 👤 User |
| POST | `/cart` | Add product to cart | 👤 User |
| PUT | `/cart/{productId}` | Update quantity | 👤 User |
| DELETE | `/cart/{productId}` | Remove product from cart | 👤 User |
| DELETE | `/cart` | Clear entire cart | 👤 User |

### Add to Cart Request
```json
{
  "productId": "uuid-of-product",
  "quantity": 2
}
```

### Update Quantity Request
```json
{
  "quantity": 5
}
```

### Cart Response
```json
{
  "items": [
    {
      "id": "cart-item-uuid",
      "productId": "product-uuid",
      "productName": "iPhone 15 Pro",
      "productImage": "https://...",
      "price": 999.99,
      "discountedPrice": 899.99,
      "quantity": 2,
      "subtotal": 1799.98
    }
  ],
  "totalItems": 2,
  "totalAmount": 1799.98
}
```

---

## 📋 Order Endpoints

### User Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/orders` | Create order (checkout) | 👤 User |
| GET | `/orders` | Get my orders | 👤 User |
| GET | `/orders/{id}` | Get order details | 👤 User |
| POST | `/orders/{id}/cancel` | Cancel order | 👤 User |

### Admin Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/orders/admin/all` | Get all orders | 🔒 Admin |
| GET | `/orders/admin/status/{status}` | Filter orders by status | 🔒 Admin |
| GET | `/orders/admin/{id}` | View any order | 🔒 Admin |
| PATCH | `/orders/admin/{id}/status?status={status}` | Update order status | 🔒 Admin |
| PATCH | `/orders/admin/{id}/payment?status={status}` | Update payment status | 🔒 Admin |

### Create Order Request
```json
{
  "addressId": "uuid-of-address",
  "paymentMethod": "COD",
  "notes": "Please call before delivery"
}
```

### Order Status Flow
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED → COMPLETED
                                            ↘ CANCELLED
                                            ↘ RETURNED
```

### Payment Methods
- `COD` - Cash on Delivery
- `CARD` - Credit/Debit Card
- `BANK_TRANSFER` - Bank Transfer
- `E_WALLET` - Digital Wallet

### Payment Status
- `PENDING` - Awaiting payment
- `PAID` - Payment received
- `FAILED` - Payment failed
- `REFUNDED` - Payment refunded

---

## ⭐ Review Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/reviews/product/{productId}` | Get product reviews | 🌐 Public |
| POST | `/reviews` | Create review | 👤 User |
| PUT | `/reviews/{id}` | Update my review | 👤 User |
| DELETE | `/reviews/{id}` | Delete my review | 👤 User |
| GET | `/reviews/my` | Get my reviews | 👤 User |

### Create Review Request
```json
{
  "productId": "uuid-of-product",
  "rating": 5,
  "comment": "Excellent product!"
}
```

### Product Review Response
```json
{
  "averageRating": 4.5,
  "totalReviews": 128,
  "reviews": [
    {
      "id": "review-uuid",
      "userId": "user-uuid",
      "userName": "John Doe",
      "rating": 5,
      "comment": "Excellent product!",
      "createdAt": "2026-01-19T10:30:00Z"
    }
  ]
}
```

---

## 💰 Sales (POS) Endpoints - Walk-in Sales

All Sales endpoints require **Admin** authentication.

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/sales` | Create a walk-in sale | 🔒 Admin |
| GET | `/sales/{id}` | Get sale details | 🔒 Admin |
| GET | `/sales` | Get all sales (paginated) | 🔒 Admin |
| GET | `/sales/today` | Get today's sales | 🔒 Admin |
| GET | `/sales/range?startDate=&endDate=` | Get sales by date range | 🔒 Admin |
| GET | `/sales/summary/today` | Get today's sales summary | 🔒 Admin |
| GET | `/sales/summary?startDate=&endDate=` | Get sales summary for range | 🔒 Admin |
| GET | `/sales/product/{productId}/stats` | Get sales stats for a product | 🔒 Admin |
| GET | `/sales/top-selling?limit=10` | Get top selling products | 🔒 Admin |
| GET | `/sales/top-selling/range?startDate=&endDate=&limit=10` | Top selling in date range | 🔒 Admin |

### Create Sale Request
```json
{
  "customerName": "Walk-in Customer",
  "customerPhone": "0123456789",
  "items": [
    {
      "productId": "uuid-of-product",
      "quantity": 2
    }
  ],
  "discountAmount": 0,
  "paymentMethod": "CASH",
  "notes": "Paid in cash"
}
```

### Product Sales Stats Response
```json
{
  "productId": "uuid",
  "productName": "iPhone 15 Pro",
  "totalQuantitySold": 150,
  "totalRevenue": 149850.00,
  "totalProfit": 14985.00,
  "currentStock": 45
}
```

### Top Selling Products Response
```json
[
  {
    "productId": "uuid",
    "productName": "iPhone 15 Pro",
    "totalQuantitySold": 150
  },
  {
    "productId": "uuid",
    "productName": "Samsung Galaxy S24",
    "totalQuantitySold": 120
  }
]
```

### Sales Summary Response
```json
{
  "totalSales": 25,
  "totalRevenue": 5000.00,
  "totalCost": 3500.00,
  "totalProfit": 1500.00,
  "profitMargin": 30.0,
  "periodStart": "2026-01-26T00:00:00Z",
  "periodEnd": "2026-01-27T00:00:00Z"
}
```

---

## 🔐 Authentication

### Using JWT Token
Include the JWT token in the `Authorization` header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Auth Legend
| Symbol | Meaning |
|--------|---------|
| 🌐 Public | No authentication required |
| 👤 User | Requires valid JWT token (USER or ADMIN role) |
| 🔒 Admin | Requires valid JWT token with ADMIN role |

---

## 📄 Standard Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "createdAt": "2026-01-19T10:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "errors": ["Validation error 1", "Validation error 2"],
  "createdAt": "2026-01-19T10:30:00Z"
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "isFirst": true,
    "isLast": false
  }
}
```

---

## ⚙️ Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/rolly_shop_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | - |
| `JWT_SECRET` | JWT signing key | - |
| `AWS_ACCESS_KEY` | AWS access key | - |
| `AWS_SECRET_KEY` | AWS secret key | - |
| `AWS_REGION` | AWS region | `ap-southeast-2` |
| `S3_BUCKET` | S3 bucket name | `rolly-shop-bucket` |
| `REMOVEBG_API_KEY` | Remove.bg API key | - |

---

## 🆕 NEW FEATURES

### 1. 🖼️ Multi-Image Support API

Products can now have multiple images with ordering and primary image selection.

**Endpoints:**

```http
# Get all images for a product
GET /api/v1/products/{productId}/images

# Add a new image
POST /api/v1/products/{productId}/images
Body: {
  "url": "https://cdn.example.com/image.jpg",
  "isPrimary": false,
  "displayOrder": 1
}

# Set an image as primary
PUT /api/v1/products/{productId}/images/{imageId}/set-primary

# Reorder images
PUT /api/v1/products/{productId}/images/reorder
Body: {
  "imageOrders": [
    { "imageId": "uuid", "displayOrder": 0 },
    { "imageId": "uuid", "displayOrder": 1 }
  ]
}

# Delete an image (minimum 1 image required)
DELETE /api/v1/products/{productId}/images/{imageId}
```

**Features:**
- ✅ Multiple images per product
- ✅ Primary image selection (automatically unsets others)
- ✅ Custom ordering/sorting
- ✅ Automatic primary reassignment on delete
- ✅ Prevents deleting last image

---

### 2. 📜 Stock History & Audit Trail

Complete inventory change tracking with detailed audit logs.

**Endpoints:**

```http
# Get stock history for a product
GET /api/v1/products/{productId}/stock-history
  ?page=0&size=20
  &startDate=2026-01-01T00:00:00Z
  &endDate=2026-01-31T23:59:59Z

# Manually adjust stock
POST /api/v1/products/{productId}/stock-adjustment
Body: {
  "adjustment": -5,  // negative = decrease, positive = increase
  "adjustmentType": "DAMAGE",  // SALE, RESTOCK, DAMAGE, MANUAL, RETURN, CORRECTION
  "reason": "3 units damaged during inspection"
}

# Get summary of all stock changes
GET /api/v1/products/stock-history/summary
  ?startDate=2026-01-01T00:00:00Z
  &endDate=2026-01-31T23:59:59Z
  &adjustmentType=SALE  // optional filter
```

**Adjustment Types:**
- `SALE` - Stock sold to customers
- `RESTOCK` - New inventory received
- `DAMAGE` - Damaged/broken items
- `MANUAL` - Manual adjustment
- `RETURN` - Customer returns
- `CORRECTION` - Inventory correction

**Features:**
- ✅ Complete audit trail (who, when, why)
- ✅ Tracks previous/new stock values
- ✅ Links to related records (sales, orders)
- ✅ Prevents negative stock
- ✅ Automatic user tracking via JWT
- ✅ Summary statistics by adjustment type

**Response Example:**
```json
{
  "id": "uuid",
  "productId": "uuid",
  "productName": "BB Luxury",
  "previousStock": 60,
  "newStock": 55,
  "adjustment": -5,
  "adjustmentType": "SALE",
  "reason": "Sold 5 units",
  "referenceId": "sale-uuid",
  "referenceType": "SALE",
  "updatedBy": "admin@example.com",
  "updatedByName": "Admin User",
  "createdAt": "2026-01-30T14:30:00Z"
}
```

---

### 3. 📥 Export to Excel/CSV

Export product inventory data with advanced filtering.

**Endpoint:**

```http
# Export products
GET /api/v1/products/export
  ?format=excel  # or 'csv'
  &brandId=uuid  # optional filter
  &categoryId=uuid  # optional filter
  &lowStock=true  # optional: products with stock <= 10
  &search=collagen  # optional: search by name
  &sortBy=name  # optional: sort field
  &direction=asc  # optional: asc or desc

Authorization: Bearer {admin-token}
```

**Export Formats:**

**CSV:**
- Single file with all product data
- UTF-8 with BOM (Excel compatible)
- Includes: ID, Name, Barcode, Category, Brand, Prices, Stock, Sales, Revenue, Profit

**Excel (.xlsx):**
- **Sheet 1: "Products"** - Complete product data table
  - ID, Name, Barcode, Category, Brand
  - Cost Price, Selling Price, Discount, Final Price
  - Profit per Unit, Stock Quantity, Stock Value
  - Total Sold, Total Revenue, Total Profit
  - Image URL, Created/Updated timestamps

- **Sheet 2: "Summary"** - Business statistics
  - Total products count
  - Total stock value
  - Total potential profit
  - Low stock count (<= 10 units)

- **Sheet 3: "Low Stock Alert"** - Reorder list
  - Products with stock <= 10
  - Sorted by urgency (0 stock first)
  - Includes Category, Brand, Stock Quantity
  - Reorder status (URGENT or Soon)

**Features:**
- ✅ Filter by brand, category, low stock, search
- ✅ Custom sorting
- ✅ Auto-sized columns
- ✅ Formatted headers
- ✅ Sales data integration
- ✅ Multi-sheet Excel with summary
- ✅ Direct file download

**Example Usage:**
```bash
# Export low stock products to Excel
curl -X GET "http://localhost:8080/api/v1/products/export?format=excel&lowStock=true&sortBy=stockQuantity&direction=asc" \
  -H "Authorization: Bearer {token}" \
  --output products-low-stock.xlsx

# Export all products to CSV
curl -X GET "http://localhost:8080/api/v1/products/export?format=csv" \
  -H "Authorization: Bearer {token}" \
  --output products.csv
```

---

## 🏃 Running the Application

```bash
# Set environment variables
export JWT_SECRET=your-secret-key
export DB_PASSWORD=your-db-password
export AWS_ACCESS_KEY=your-aws-key
export AWS_SECRET_KEY=your-aws-secret
export REMOVEBG_API_KEY=your-removebg-key

# Run with Gradle
./gradlew bootRun
```

---

## 📊 Database Schema

```
┌─────────────┐       ┌──────────────┐
│   BRANDS    │       │  CATEGORIES  │
└──────┬──────┘       └──────┬───────┘
       │                     │
       └──────────┬──────────┘
                  ▼
         ┌───────────────┐
         │   PRODUCTS    │◄────┐
         └───────┬───────┘     │
                 │              │
    ┌────────────┼──────────────┼────────┐
    ▼            ▼              ▼        ▼
┌───────┐  ┌───────────┐  ┌─────────┐  ┌────────────────┐
│ CART  │  │ORDER_ITEMS│  │ REVIEWS │  │ PRODUCT_IMAGES │ 🆕
└───┬───┘  └─────┬─────┘  └────┬────┘  └────────────────┘
    │            │             │
    │            │             │       ┌────────────────┐
    │            │             │       │ STOCK_HISTORY  │ 🆕
    │            │             │       └────────┬───────┘
    │            │             │                │
    └────────────┼─────────────┴────────────────┘
                 ▼
         ┌───────────────┐
         │     USERS     │
         └───────┬───────┘
                 │
       ┌─────────┴─────────┐
       ▼                   ▼
┌─────────────┐      ┌───────────┐
│  ADDRESSES  │◄────►│  ORDERS   │
└─────────────┘      └───────────┘

🆕 New Tables:
  • PRODUCT_IMAGES: Multi-image support with ordering
  • STOCK_HISTORY: Complete audit trail for inventory changes
```

---

## 📝 License

MIT License

