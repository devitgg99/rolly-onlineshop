package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.Refund
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefundRepository : JpaRepository<Refund, UUID> {
    
    // Get all refunds for a specific sale
    fun findBySaleId(saleId: UUID): List<Refund>
    
    // Get all refunds with pagination
    override fun findAll(pageable: Pageable): Page<Refund>
    
    // Check if a sale has any refunds
    fun existsBySaleId(saleId: UUID): Boolean
}
