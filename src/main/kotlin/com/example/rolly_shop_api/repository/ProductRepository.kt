package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ProductRepository : JpaRepository<Product, UUID> {
    fun findByBrandId(brandId: UUID, pageable: Pageable): Page<Product>
    fun findByCategoryId(categoryId: UUID, pageable: Pageable): Page<Product>
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Product>

    // Search by name OR barcode
    fun findByNameContainingIgnoreCaseOrBarcodeContainingIgnoreCase(
        name: String,
        barcode: String,
        pageable: Pageable
    ): Page<Product>

    // Filter by category AND search by name OR barcode
    fun findByCategoryIdAndNameContainingIgnoreCaseOrBarcodeContainingIgnoreCase(
        categoryId: UUID,
        name: String,
        barcode: String,
        pageable: Pageable
    ): Page<Product>

    // Find by barcode
    fun findByBarcode(barcode: String): Optional<Product>

    // Check if barcode exists (for validation)
    fun existsByBarcode(barcode: String): Boolean

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    fun getAverageRating(productId: UUID): Double?

    // Inventory stats
    @Query("SELECT COUNT(p) FROM Product p")
    fun countAllProducts(): Long

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= :threshold")
    fun countLowStock(threshold: Int): Long

    @Query("SELECT COALESCE(SUM(p.costPrice * p.stockQuantity), 0) FROM Product p")
    fun getTotalInventoryValue(): java.math.BigDecimal

    @Query("SELECT COALESCE(SUM((p.price * (1 - p.discountPercent / 100.0) - p.costPrice) * p.stockQuantity), 0) FROM Product p")
    fun getTotalPotentialProfit(): java.math.BigDecimal

    // Get low stock products
    fun findByStockQuantityLessThanEqual(threshold: Int, pageable: Pageable): Page<Product>
}
