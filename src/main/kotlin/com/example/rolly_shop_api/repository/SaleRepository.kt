package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.PaymentMethod
import com.example.rolly_shop_api.model.entity.Sale
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Repository
interface SaleRepository : JpaRepository<Sale, UUID>, JpaSpecificationExecutor<Sale> {

    // Get sales within date range
    fun findByCreatedAtBetween(start: Instant, end: Instant, pageable: Pageable): Page<Sale>

    // Get today's sales
    @Query("SELECT s FROM Sale s WHERE s.createdAt >= :startOfDay")
    fun findTodaySales(startOfDay: Instant, pageable: Pageable): Page<Sale>

    // Total profit for date range
    @Query("SELECT COALESCE(SUM(s.profit), 0) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    fun getTotalProfit(start: Instant, end: Instant): BigDecimal

    // Total revenue for date range
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    fun getTotalRevenue(start: Instant, end: Instant): BigDecimal

    // Count sales for date range
    fun countByCreatedAtBetween(start: Instant, end: Instant): Long

    // Get sales by admin
    fun findBySoldByUserId(userId: UUID, pageable: Pageable): Page<Sale>

    // ==================== ANALYTICS QUERIES ====================

    // Sales by payment method
    @Query("""
        SELECT s.paymentMethod, COUNT(s), COALESCE(SUM(s.totalAmount), 0)
        FROM Sale s
        WHERE s.createdAt BETWEEN :start AND :end
        GROUP BY s.paymentMethod
    """)
    fun getSalesByPaymentMethod(start: Instant, end: Instant): List<Array<Any>>

    // Sales by hour of day
    @Query("""
        SELECT EXTRACT(HOUR FROM s.createdAt), COUNT(s), COALESCE(SUM(s.totalAmount), 0)
        FROM Sale s
        WHERE s.createdAt BETWEEN :start AND :end
        GROUP BY EXTRACT(HOUR FROM s.createdAt)
        ORDER BY EXTRACT(HOUR FROM s.createdAt)
    """)
    fun getSalesByHour(start: Instant, end: Instant): List<Array<Any>>

    // Top customers
    @Query("""
        SELECT s.customerName, s.customerPhone, COALESCE(SUM(s.totalAmount), 0), COUNT(s)
        FROM Sale s
        WHERE s.createdAt BETWEEN :start AND :end
        AND s.customerName IS NOT NULL
        GROUP BY s.customerName, s.customerPhone
        ORDER BY SUM(s.totalAmount) DESC
    """)
    fun getTopCustomers(start: Instant, end: Instant, pageable: Pageable): List<Array<Any>>
}
