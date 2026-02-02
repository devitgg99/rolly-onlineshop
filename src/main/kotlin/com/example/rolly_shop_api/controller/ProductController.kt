package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.ProductRequest
import com.example.rolly_shop_api.model.dto.response.*
import com.example.rolly_shop_api.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product catalog endpoints")
class ProductController(
    private val productService: ProductService
) {
    // ==================== PUBLIC ENDPOINTS (Customer View - NO cost price) ====================

    @GetMapping
    @SecurityRequirements
    @Operation(
        summary = "Get all products (paginated)",
        description = "🌐 PUBLIC - Browse all products. Shows selling price only."
    )
    fun getAll(
        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") size: Int,
        @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") sortBy: String,
        @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") direction: String
    ): BaseResponse<PageResponse<ProductSimpleResponse>> {
        val sort = if (direction == "asc") Sort.by(sortBy).ascending() else Sort.by(sortBy).descending()
        val pageable = PageRequest.of(page, size, sort)
        return BaseResponse.success(productService.getAll(pageable), "Products retrieved")
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(
        summary = "Get product details",
        description = "🌐 PUBLIC - Get product details. Shows selling price only."
    )
    fun getById(@PathVariable id: UUID): BaseResponse<ProductResponse> =
        BaseResponse.success(productService.getById(id), "Product found")

    @GetMapping("/brand/{brandId}")
    @SecurityRequirements
    @Operation(
        summary = "Get products by brand",
        description = "🌐 PUBLIC - Filter products by brand ID"
    )
    fun getByBrand(
        @PathVariable brandId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): BaseResponse<PageResponse<ProductSimpleResponse>> {
        val pageable = PageRequest.of(page, size)
        return BaseResponse.success(productService.getByBrand(brandId, pageable), "Products by brand")
    }

    @GetMapping("/category/{categoryId}")
    @SecurityRequirements
    @Operation(
        summary = "Get products by category",
        description = "🌐 PUBLIC - Filter products by category ID"
    )
    fun getByCategory(
        @PathVariable categoryId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): BaseResponse<PageResponse<ProductSimpleResponse>> {
        val pageable = PageRequest.of(page, size)
        return BaseResponse.success(productService.getByCategory(categoryId, pageable), "Products by category")
    }

    @GetMapping("/search")
    @SecurityRequirements
    @Operation(
        summary = "Search products",
        description = "🌐 PUBLIC - Search products by name"
    )
    fun search(
        @Parameter(description = "Search query") @RequestParam q: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): BaseResponse<PageResponse<ProductSimpleResponse>> {
        val pageable = PageRequest.of(page, size)
        return BaseResponse.success(productService.search(q, pageable), "Search results")
    }

    // ==================== ADMIN ENDPOINTS (includes cost price & profit) ====================

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get inventory statistics",
        description = """
            🔒 ADMIN ONLY - Get inventory dashboard stats:
            - Total products count
            - Total inventory value (cost price × stock)
            - Total potential profit ((selling price - cost) × stock)
            - Low stock count (products below threshold)
        """
    )
    fun getInventoryStats(
        @Parameter(description = "Low stock threshold (default 10)")
        @RequestParam(defaultValue = "10") lowStockThreshold: Int
    ): BaseResponse<InventoryStatsResponse> =
        BaseResponse.success(productService.getInventoryStats(lowStockThreshold), "Inventory stats")

    @GetMapping("/admin/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get low stock products",
        description = "🔒 ADMIN ONLY - Get products with stock quantity at or below threshold"
    )
    fun getLowStockProducts(
        @Parameter(description = "Stock threshold") @RequestParam(defaultValue = "10") threshold: Int,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): BaseResponse<PageResponse<ProductAdminSimpleResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("stockQuantity").ascending())
        return BaseResponse.success(productService.getLowStockProducts(threshold, pageable), "Low stock products")
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all products (Admin view)",
        description = "🔒 ADMIN ONLY - Get all products with cost price and profit"
    )
    fun getAllAdmin(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") direction: String
    ): BaseResponse<PageResponse<ProductAdminSimpleResponse>> {
        val sort = if (direction == "asc") Sort.by(sortBy).ascending() else Sort.by(sortBy).descending()
        val pageable = PageRequest.of(page, size, sort)
        return BaseResponse.success(productService.getAllAdmin(pageable), "Products retrieved (admin)")
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get product details (Admin view)",
        description = "🔒 ADMIN ONLY - Get product with cost price and profit"
    )
    fun getByIdAdmin(@PathVariable id: UUID): BaseResponse<ProductAdminResponse> =
        BaseResponse.success(productService.getByIdAdmin(id), "Product found (admin)")

    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Find product by barcode",
        description = "🔒 ADMIN ONLY - Scan barcode to find product (for POS/inventory)"
    )
    fun getByBarcode(@PathVariable barcode: String): BaseResponse<ProductAdminResponse> =
        BaseResponse.success(productService.getByBarcode(barcode), "Product found by barcode")

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create product",
        description = "🔒 ADMIN ONLY - Create new product with cost price and selling price"
    )
    fun create(@Valid @RequestBody request: ProductRequest): BaseResponse<ProductAdminResponse> =
        BaseResponse.success(productService.create(request), "Product created")

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update product",
        description = "🔒 ADMIN ONLY - Update existing product"
    )
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ProductRequest
    ): BaseResponse<ProductAdminResponse> =
        BaseResponse.success(productService.update(id, request), "Product updated")

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete product",
        description = "🔒 ADMIN ONLY - Delete product by ID"
    )
    fun delete(@PathVariable id: UUID): BaseResponse<Unit> {
        productService.delete(id)
        return BaseResponse.ok("Product deleted")
    }
}
