package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.RefundItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefundItemRepository : JpaRepository<RefundItem, UUID> {
    fun findByRefundId(refundId: UUID): List<RefundItem>
}
