package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.Brand
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BrandRepository : JpaRepository<Brand, UUID> {
    fun existsByName(name: String): Boolean
    fun findByNameContainingIgnoreCase(name: String): List<Brand>
}
