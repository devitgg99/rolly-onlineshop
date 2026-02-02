package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.CategoryRequest
import com.example.rolly_shop_api.model.dto.response.BaseResponse
import com.example.rolly_shop_api.model.dto.response.CategoryResponse
import com.example.rolly_shop_api.service.CategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Category management endpoints")
class CategoryController(
    private val categoryService: CategoryService
) {
    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping
    @SecurityRequirements
    @Operation(
        summary = "Get all categories",
        description = "🌐 PUBLIC - Returns all categories"
    )
    fun getAll(): BaseResponse<List<CategoryResponse>> =
        BaseResponse.success(categoryService.getAll(), "Categories retrieved")

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(
        summary = "Get category by ID",
        description = "🌐 PUBLIC - Get single category details"
    )
    fun getById(@PathVariable id: UUID): BaseResponse<CategoryResponse> =
        BaseResponse.success(categoryService.getById(id), "Category found")

    @GetMapping("/root")
    @SecurityRequirements
    @Operation(
        summary = "Get root categories",
        description = "🌐 PUBLIC - Get top-level categories (no parent)"
    )
    fun getRootCategories(): BaseResponse<List<CategoryResponse>> =
        BaseResponse.success(categoryService.getRootCategories(), "Root categories retrieved")

    @GetMapping("/{parentId}/subcategories")
    @SecurityRequirements
    @Operation(
        summary = "Get subcategories",
        description = "🌐 PUBLIC - Get child categories of a parent category"
    )
    fun getSubcategories(@PathVariable parentId: UUID): BaseResponse<List<CategoryResponse>> =
        BaseResponse.success(categoryService.getSubcategories(parentId), "Subcategories retrieved")

    // ==================== ADMIN ENDPOINTS ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create category",
        description = "🔒 ADMIN ONLY - Create new category. Set parentId for subcategory."
    )
    fun create(@Valid @RequestBody request: CategoryRequest): BaseResponse<CategoryResponse> =
        BaseResponse.success(categoryService.create(request), "Category created")

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update category",
        description = "🔒 ADMIN ONLY - Update existing category"
    )
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CategoryRequest
    ): BaseResponse<CategoryResponse> =
        BaseResponse.success(categoryService.update(id, request), "Category updated")

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete category",
        description = "🔒 ADMIN ONLY - Delete category by ID"
    )
    fun delete(@PathVariable id: UUID): BaseResponse<Unit> {
        categoryService.delete(id)
        return BaseResponse.ok("Category deleted")
    }
}
