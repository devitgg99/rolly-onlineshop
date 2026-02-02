package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.Address
import java.util.*

data class AddressResponse(
    val id: UUID,
    val fullName: String,
    val phoneNumber: String,
    val addressLine: String,
    val city: String,
    val province: String?,
    val postalCode: String?,
    val isDefault: Boolean
) {
    companion object {
        fun from(address: Address) = AddressResponse(
            id = address.id!!,
            fullName = address.fullName,
            phoneNumber = address.phoneNumber,
            addressLine = address.addressLine,
            city = address.city,
            province = address.province,
            postalCode = address.postalCode,
            isDefault = address.isDefault
        )
    }
}

