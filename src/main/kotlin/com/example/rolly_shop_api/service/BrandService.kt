package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.BrandRequest
import com.example.rolly_shop_api.model.dto.response.BrandResponse
import java.util.*

interface BrandService {
    fun create(request: BrandRequest): BrandResponse
    fun update(id: UUID, request: BrandRequest): BrandResponse
    fun delete(id: UUID)
    fun getById(id: UUID): BrandResponse
    fun getAll(): List<BrandResponse>
}
