package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.AddressRequest
import com.example.rolly_shop_api.model.dto.response.AddressResponse
import java.util.*

interface AddressService {
    fun create(userId: UUID, request: AddressRequest): AddressResponse
    fun update(userId: UUID, addressId: UUID, request: AddressRequest): AddressResponse
    fun delete(userId: UUID, addressId: UUID)
    fun getById(userId: UUID, addressId: UUID): AddressResponse
    fun getAllByUser(userId: UUID): List<AddressResponse>
    fun setDefault(userId: UUID, addressId: UUID): AddressResponse
}

