package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.AddressRequest
import com.example.rolly_shop_api.model.dto.response.AddressResponse
import com.example.rolly_shop_api.model.dto.response.BaseResponse
import com.example.rolly_shop_api.service.AddressService
import com.example.rolly_shop_api.service.CurrentUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/addresses")
@Tag(name = "Addresses", description = "User shipping address management")
class AddressController(
    private val addressService: AddressService,
    private val currentUserService: CurrentUserService
) {
    // ==================== USER ENDPOINTS (Authenticated) ====================

    @GetMapping
    @Operation(
        summary = "Get my addresses",
        description = "👤 USER - Get all shipping addresses for logged-in user"
    )
    fun getMyAddresses(): BaseResponse<List<AddressResponse>> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(addressService.getAllByUser(userId), "Addresses retrieved")
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get address by ID",
        description = "👤 USER - Get specific address details"
    )
    fun getById(@PathVariable id: UUID): BaseResponse<AddressResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(addressService.getById(userId, id), "Address found")
    }

    @PostMapping
    @Operation(
        summary = "Add new address",
        description = "👤 USER - Add new shipping address. First address is auto-set as default."
    )
    fun create(@Valid @RequestBody request: AddressRequest): BaseResponse<AddressResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(addressService.create(userId, request), "Address created")
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update address",
        description = "👤 USER - Update existing address"
    )
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AddressRequest
    ): BaseResponse<AddressResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(addressService.update(userId, id, request), "Address updated")
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete address",
        description = "👤 USER - Delete shipping address"
    )
    fun delete(@PathVariable id: UUID): BaseResponse<Unit> {
        val userId = currentUserService.getCurrentUserId()
        addressService.delete(userId, id)
        return BaseResponse.ok("Address deleted")
    }

    @PatchMapping("/{id}/default")
    @Operation(
        summary = "Set as default address",
        description = "👤 USER - Set address as default shipping address"
    )
    fun setDefault(@PathVariable id: UUID): BaseResponse<AddressResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(addressService.setDefault(userId, id), "Default address updated")
    }
}
