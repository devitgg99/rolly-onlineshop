package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.BrandRequest
import com.example.rolly_shop_api.model.dto.response.BaseResponse
import com.example.rolly_shop_api.model.dto.response.BrandResponse
import com.example.rolly_shop_api.service.BrandService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/brands")
@Tag(name = "Brands", description = "Brand management endpoints")
class BrandController(
    private val brandService: BrandService
) {
    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping
    @SecurityRequirements
    @Operation(
        summary = "Get all brands",
        description = "🌐 PUBLIC - Returns list of all brands"
    )
    fun getAll(): BaseResponse<List<BrandResponse>> =
        BaseResponse.success(brandService.getAll(), "Brands retrieved")

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(
        summary = "Get brand by ID",
        description = "🌐 PUBLIC - Get single brand details by ID"
    )
    fun getById(@PathVariable id: UUID): BaseResponse<BrandResponse> =
        BaseResponse.success(brandService.getById(id), "Brand found")

    // ==================== ADMIN ENDPOINTS ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create new brand",
        description = "🔒 ADMIN ONLY - Create a new brand"
    )
    fun create(@Valid @RequestBody request: BrandRequest): BaseResponse<BrandResponse> =
        BaseResponse.success(brandService.create(request), "Brand created")

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update brand",
        description = "🔒 ADMIN ONLY - Update existing brand by ID"
    )
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: BrandRequest
    ): BaseResponse<BrandResponse> =
        BaseResponse.success(brandService.update(id, request), "Brand updated")

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete brand",
        description = "🔒 ADMIN ONLY - Delete brand by ID"
    )
    fun delete(@PathVariable id: UUID): BaseResponse<Unit> {
        brandService.delete(id)
        return BaseResponse.ok("Brand deleted")
    }
}
