package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.dto.request.RefundMethod
import com.example.rolly_shop_api.model.entity.Refund
import com.example.rolly_shop_api.model.entity.RefundItem
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Full refund response with all details
 */
data class RefundResponse(
    val id: UUID,
    val saleId: UUID,
    val items: List<RefundItemResponse>,
    val refundAmount: BigDecimal,
    val refundMethod: RefundMethod,
    val processedBy: String?,
    val notes: String?,
    val createdAt: Instant
) {
    companion object {
        fun from(refund: Refund) = RefundResponse(
            id = refund.id!!,
            saleId = refund.sale.id!!,
            items = refund.items.map { RefundItemResponse.from(it) },
            refundAmount = refund.refundAmount,
            refundMethod = refund.refundMethod,
            processedBy = refund.processedBy?.fullName,
            notes = refund.notes,
            createdAt = refund.createdAt
        )
    }
}

/**
 * Refund item details
 */
data class RefundItemResponse(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val reason: String?
) {
    companion object {
        fun from(item: RefundItem) = RefundItemResponse(
            id = item.id!!,
            productId = item.product.id!!,
            productName = item.product.name,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            subtotal = item.subtotal,
            reason = item.reason
        )
    }
}

/**
 * Simple refund for list view
 */
data class RefundSimpleResponse(
    val id: UUID,
    val saleId: UUID,
    val refundAmount: BigDecimal,
    val refundMethod: RefundMethod,
    val itemCount: Int,
    val createdAt: Instant
) {
    companion object {
        fun from(refund: Refund) = RefundSimpleResponse(
            id = refund.id!!,
            saleId = refund.sale.id!!,
            refundAmount = refund.refundAmount,
            refundMethod = refund.refundMethod,
            itemCount = refund.items.size,
            createdAt = refund.createdAt
        )
    }
}
