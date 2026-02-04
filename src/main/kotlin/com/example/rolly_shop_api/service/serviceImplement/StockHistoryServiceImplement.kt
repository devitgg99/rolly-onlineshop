package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.model.dto.request.StockAdjustmentRequest
import com.example.rolly_shop_api.model.dto.response.*
import com.example.rolly_shop_api.model.entity.AdjustmentType
import com.example.rolly_shop_api.model.entity.StockHistory
import com.example.rolly_shop_api.repository.ProductRepository
import com.example.rolly_shop_api.repository.StockHistoryRepository
import com.example.rolly_shop_api.service.CurrentUserService
import com.example.rolly_shop_api.service.StockHistoryService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class StockHistoryServiceImplement(
    private val stockHistoryRepository: StockHistoryRepository,
    private val productRepository: ProductRepository,
    private val currentUserService: CurrentUserService
) : StockHistoryService {

    override fun getStockHistory(
        productId: UUID,
        startDate: Instant?,
        endDate: Instant?,
        pageable: Pageable
    ): Page<StockHistoryResponse> {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        
        val historyPage = if (startDate != null && endDate != null) {
            stockHistoryRepository.findByProductIdAndCreatedAtBetween(
                productId,
                startDate,
                endDate,
                pageable
            )
        } else {
            stockHistoryRepository.findByProductId(productId, pageable)
        }
        
        return historyPage.map { StockHistoryResponse.from(it) }
    }

    @Transactional
    override fun adjustStock(productId: UUID, request: StockAdjustmentRequest): StockAdjustmentResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        
        val currentUser = currentUserService.getCurrentUser()
        val previousStock = product.stockQuantity
        val newStock = previousStock + request.adjustment
        
        // Validate: don't allow negative stock
        if (newStock < 0) {
            throw IllegalArgumentException("Stock cannot be negative. Current stock: $previousStock, adjustment: ${request.adjustment}")
        }
        
        // Update product stock
        product.stockQuantity = newStock
        product.updatedAt = Instant.now()
        productRepository.save(product)
        
        // Create history entry
        val historyEntry = StockHistory(
            product = product,
            productName = product.name,
            previousStock = previousStock,
            newStock = newStock,
            adjustment = request.adjustment,
            adjustmentType = request.adjustmentType,
            reason = request.reason,
            updatedBy = currentUser.email ?: currentUser.phoneNumber ?: "unknown",
            updatedByName = currentUser.fullName
        )
        
        val savedHistory = stockHistoryRepository.save(historyEntry)
        
        return StockAdjustmentResponse(
            productId = productId.toString(),
            previousStock = previousStock,
            newStock = newStock,
            adjustment = request.adjustment,
            adjustmentType = request.adjustmentType,
            reason = request.reason,
            historyEntryId = savedHistory.id.toString(),
            updatedBy = currentUser.email ?: currentUser.phoneNumber ?: "unknown",
            updatedAt = savedHistory.createdAt
        )
    }

    override fun getStockHistorySummary(
        startDate: Instant,
        endDate: Instant,
        adjustmentType: AdjustmentType?
    ): StockHistorySummaryResponse {
        val totalAdjustments = stockHistoryRepository.countAdjustmentsBetween(startDate, endDate)
        
        // Get counts by adjustment type
        val byTypeResults = stockHistoryRepository.countByAdjustmentTypeBetween(startDate, endDate)
        val byType = byTypeResults.associate { row ->
            val type = row[0] as AdjustmentType
            val count = row[1] as Long
            type.name to count
        }
        
        // Get top products
        val topProductsData = stockHistoryRepository.getTopProductsByAdjustments(
            startDate,
            endDate,
            PageRequest.of(0, 10)
        )
        
        val topProducts = topProductsData.map { row ->
            TopProductAdjustment(
                productId = row[0].toString(),
                productName = row[1] as String,
                totalAdjustments = row[2] as Long,
                netChange = (row[3] as Long).toInt()
            )
        }
        
        return StockHistorySummaryResponse(
            totalAdjustments = totalAdjustments,
            byType = byType,
            topProducts = topProducts
        )
    }
}
