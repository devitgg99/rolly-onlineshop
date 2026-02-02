package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.CategoryRequest
import com.example.rolly_shop_api.model.dto.response.CategoryResponse
import java.util.*

interface CategoryService {
    fun create(request: CategoryRequest): CategoryResponse
    fun update(id: UUID, request: CategoryRequest): CategoryResponse
    fun delete(id: UUID)
    fun getById(id: UUID): CategoryResponse
    fun getAll(): List<CategoryResponse>
    fun getRootCategories(): List<CategoryResponse>
    fun getSubcategories(parentId: UUID): List<CategoryResponse>
}
