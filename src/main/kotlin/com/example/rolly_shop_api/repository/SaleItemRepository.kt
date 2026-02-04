package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.SaleItem
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface SaleItemRepository : JpaRepository<SaleItem, UUID> {

    // Total quantity sold for a specific product (all time)
    @Query("SELECT COALESCE(SUM(si.quantity), 0) FROM SaleItem si WHERE si.product.id = :productId")
    fun getTotalQuantitySold(productId: UUID): Int

    // Total quantity sold for a product within date range
    @Query("""
        SELECT COALESCE(SUM(si.quantity), 0) 
        FROM SaleItem si 
        WHERE si.product.id = :productId 
        AND si.sale.createdAt BETWEEN :startDate AND :endDate
    """)
    fun getQuantitySoldBetween(productId: UUID, startDate: Instant, endDate: Instant): Int

    // Total revenue for a specific product (all time)
    @Query("SELECT COALESCE(SUM(si.subtotal), 0) FROM SaleItem si WHERE si.product.id = :productId")
    fun getTotalRevenueForProduct(productId: UUID): java.math.BigDecimal

    // Total profit for a specific product (all time)
    @Query("SELECT COALESCE(SUM(si.profit), 0) FROM SaleItem si WHERE si.product.id = :productId")
    fun getTotalProfitForProduct(productId: UUID): java.math.BigDecimal

    // Get top selling products (by quantity)
    @Query("""
        SELECT si.product.id, si.product.name, SUM(si.quantity) as totalQty
        FROM SaleItem si
        GROUP BY si.product.id, si.product.name
        ORDER BY totalQty DESC
    """)
    fun getTopSellingProducts(pageable: Pageable): List<Array<Any>>

    // Get top selling products within date range
    @Query("""
        SELECT si.product.id, si.product.name, SUM(si.quantity) as totalQty
        FROM SaleItem si
        WHERE si.sale.createdAt BETWEEN :startDate AND :endDate
        GROUP BY si.product.id, si.product.name
        ORDER BY totalQty DESC
    """)
    fun getTopSellingProductsBetween(startDate: Instant, endDate: Instant, pageable: Pageable): List<Array<Any>>

    // Get all sale items for a product
    fun findByProductId(productId: UUID): List<SaleItem>
}
