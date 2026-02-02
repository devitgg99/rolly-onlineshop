package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.model.dto.request.CategoryRequest
import com.example.rolly_shop_api.model.dto.response.CategoryResponse
import com.example.rolly_shop_api.model.entity.Category
import com.example.rolly_shop_api.repository.CategoryRepository
import com.example.rolly_shop_api.service.CategoryService
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class CategoryServiceImplement(
    private val categoryRepository: CategoryRepository
) : CategoryService {

    override fun create(request: CategoryRequest): CategoryResponse {
        val parent = request.parentId?.let {
            categoryRepository.findById(it)
                .orElseThrow { NoSuchElementException("Parent category not found") }
        }
        val category = Category(
            name = request.name,
            description = request.description,
            imageUrl = request.imageUrl,
            parent = parent
        )
        return CategoryResponse.from(categoryRepository.save(category))
    }

    override fun update(id: UUID, request: CategoryRequest): CategoryResponse {
        val category = categoryRepository.findById(id)
            .orElseThrow { NoSuchElementException("Category not found") }
        val parent = request.parentId?.let {
            categoryRepository.findById(it)
                .orElseThrow { NoSuchElementException("Parent category not found") }
        }
        val updated = category.copy(
            name = request.name,
            description = request.description,
            imageUrl = request.imageUrl,
            parent = parent,
            updatedAt = Instant.now()
        )
        return CategoryResponse.from(categoryRepository.save(updated))
    }

    override fun delete(id: UUID) {
        if (!categoryRepository.existsById(id)) {
            throw NoSuchElementException("Category not found")
        }
        categoryRepository.deleteById(id)
    }

    override fun getById(id: UUID): CategoryResponse {
        val category = categoryRepository.findById(id)
            .orElseThrow { NoSuchElementException("Category not found") }
        return CategoryResponse.from(category)
    }

    override fun getAll(): List<CategoryResponse> =
        categoryRepository.findAll().map { CategoryResponse.from(it) }

    override fun getRootCategories(): List<CategoryResponse> =
        categoryRepository.findByParentIsNull().map { CategoryResponse.from(it) }

    override fun getSubcategories(parentId: UUID): List<CategoryResponse> =
        categoryRepository.findByParentId(parentId).map { CategoryResponse.from(it) }
}
