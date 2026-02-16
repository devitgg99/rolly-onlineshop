package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.ProductRequest
import com.example.rolly_shop_api.model.dto.response.*
import org.springframework.data.domain.Pageable
import java.util.*

interface ProductService {
    // Public endpoints (customer view - no cost price)
    fun getById(id: UUID): ProductResponse
    fun getAll(pageable: Pageable): PageResponse<ProductSimpleResponse>
    fun getByBrand(brandId: UUID, pageable: Pageable): PageResponse<ProductSimpleResponse>
    fun getByCategory(categoryId: UUID, pageable: Pageable): PageResponse<ProductSimpleResponse>
    fun search(query: String, pageable: Pageable): PageResponse<ProductSimpleResponse>

    // Admin endpoints (includes cost price & profit)
    fun create(request: ProductRequest): ProductAdminResponse
    fun update(id: UUID, request: ProductRequest): ProductAdminResponse
    fun delete(id: UUID)
    fun getByIdAdmin(id: UUID): ProductAdminResponse
    fun getAllAdmin(pageable: Pageable): PageResponse<ProductAdminSimpleResponse>
    fun getAllAdminWithFilters(categoryId: UUID?, search: String?, pageable: Pageable): PageResponse<ProductAdminSimpleResponse>

    // Barcode lookup (for POS/scanning)
    fun getByBarcode(barcode: String): ProductAdminResponse

    // Inventory stats (dashboard)
    fun getInventoryStats(lowStockThreshold: Int = 10): InventoryStatsResponse

    // Get low stock products
    fun getLowStockProducts(threshold: Int, pageable: Pageable): PageResponse<ProductAdminSimpleResponse>

    // Admin inventory table (products with sales data)
    fun getInventoryTable(pageable: Pageable): PageResponse<ProductInventoryResponse>

    // ==================== VARIANT MANAGEMENT ====================

    // Get all variants of a product
    fun getVariants(parentProductId: UUID): List<ProductVariantInfo>

    // Get products grouped by parent (for admin view)
    fun getGroupedProducts(pageable: Pageable): PageResponse<ProductAdminSimpleResponse>

    // Check if product can be deleted (has variants or sales)
    fun canDelete(id: UUID): Boolean
}
