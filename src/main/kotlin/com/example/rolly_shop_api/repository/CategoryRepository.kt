package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CategoryRepository : JpaRepository<Category, UUID> {
    fun findByParentIsNull(): List<Category> // Root categories
    fun findByParentId(parentId: UUID): List<Category> // Subcategories
    fun existsByName(name: String): Boolean
}
