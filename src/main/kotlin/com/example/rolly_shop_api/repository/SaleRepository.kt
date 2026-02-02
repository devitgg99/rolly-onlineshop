package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.Sale
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface SaleRepository : JpaRepository<Sale, UUID> {

    // Get sales within date range
    fun findByCreatedAtBetween(start: Instant, end: Instant, pageable: Pageable): Page<Sale>

    // Get today's sales
    @Query("SELECT s FROM Sale s WHERE s.createdAt >= :startOfDay")
    fun findTodaySales(startOfDay: Instant, pageable: Pageable): Page<Sale>

    // Total profit for date range
    @Query("SELECT COALESCE(SUM(s.profit), 0) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    fun getTotalProfit(start: Instant, end: Instant): java.math.BigDecimal

    // Total revenue for date range
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    fun getTotalRevenue(start: Instant, end: Instant): java.math.BigDecimal

    // Count sales for date range
    fun countByCreatedAtBetween(start: Instant, end: Instant): Long

    // Get sales by admin
    fun findBySoldByUserId(userId: UUID, pageable: Pageable): Page<Sale>
}
