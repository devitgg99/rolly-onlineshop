package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.SaleRequest
import com.example.rolly_shop_api.model.dto.response.*
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.*

interface SaleService {
    // Create a new walk-in sale
    fun createSale(request: SaleRequest): SaleResponse

    // Get sale by ID
    fun getById(id: UUID): SaleResponse

    // Get all sales (paginated)
    fun getAll(pageable: Pageable): PageResponse<SaleSimpleResponse>

    // Get today's sales
    fun getTodaySales(pageable: Pageable): PageResponse<SaleSimpleResponse>

    // Get sales by date range
    fun getSalesByDateRange(startDate: LocalDate, endDate: LocalDate, pageable: Pageable): PageResponse<SaleSimpleResponse>

    // Get sales summary (dashboard)
    fun getSummary(startDate: LocalDate, endDate: LocalDate): SalesSummaryResponse

    // Get today's summary
    fun getTodaySummary(): SalesSummaryResponse
}
