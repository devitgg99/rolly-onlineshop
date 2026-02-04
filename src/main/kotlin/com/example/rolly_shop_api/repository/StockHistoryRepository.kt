package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.AdjustmentType
import com.example.rolly_shop_api.model.entity.StockHistory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.*

interface StockHistoryRepository : JpaRepository<StockHistory, UUID> {
    
    // Find by product with optional date range
    fun findByProductIdAndCreatedAtBetween(
        productId: UUID,
        startDate: Instant,
        endDate: Instant,
        pageable: Pageable
    ): Page<StockHistory>
    
    fun findByProductId(productId: UUID, pageable: Pageable): Page<StockHistory>
    
    // Summary queries
    @Query("SELECT COUNT(sh) FROM StockHistory sh WHERE sh.createdAt BETWEEN :startDate AND :endDate")
    fun countAdjustmentsBetween(startDate: Instant, endDate: Instant): Long
    
    @Query("SELECT sh.adjustmentType, COUNT(sh) FROM StockHistory sh WHERE sh.createdAt BETWEEN :startDate AND :endDate GROUP BY sh.adjustmentType")
    fun countByAdjustmentTypeBetween(startDate: Instant, endDate: Instant): List<Array<Any>>
    
    @Query("""
        SELECT sh.product.id, sh.productName, COUNT(sh), SUM(sh.adjustment)
        FROM StockHistory sh
        WHERE sh.createdAt BETWEEN :startDate AND :endDate
        GROUP BY sh.product.id, sh.productName
        ORDER BY COUNT(sh) DESC
    """)
    fun getTopProductsByAdjustments(startDate: Instant, endDate: Instant, pageable: Pageable): List<Array<Any>>
}
