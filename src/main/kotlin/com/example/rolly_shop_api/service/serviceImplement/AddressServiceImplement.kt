package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.model.dto.request.AddressRequest
import com.example.rolly_shop_api.model.dto.response.AddressResponse
import com.example.rolly_shop_api.model.entity.Address
import com.example.rolly_shop_api.repository.AddressRepository
import com.example.rolly_shop_api.repository.UserRepository
import com.example.rolly_shop_api.service.AddressService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AddressServiceImplement(
    private val addressRepository: AddressRepository,
    private val userRepository: UserRepository
) : AddressService {

    @Transactional
    override fun create(userId: UUID, request: AddressRequest): AddressResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        // If this is the first address or marked as default, clear other defaults
        if (request.isDefault || addressRepository.findByUserUserId(userId).isEmpty()) {
            addressRepository.clearDefaultAddress(userId)
        }

        val address = Address(
            user = user,
            fullName = request.fullName,
            phoneNumber = request.phoneNumber,
            addressLine = request.addressLine,
            city = request.city,
            province = request.province,
            postalCode = request.postalCode,
            isDefault = request.isDefault || addressRepository.findByUserUserId(userId).isEmpty()
        )
        return AddressResponse.from(addressRepository.save(address))
    }

    override fun update(userId: UUID, addressId: UUID, request: AddressRequest): AddressResponse {
        val address = addressRepository.findById(addressId)
            .orElseThrow { NoSuchElementException("Address not found") }

        if (address.user.userId != userId) {
            throw IllegalArgumentException("Address does not belong to user")
        }

        val updated = address.copy(
            fullName = request.fullName,
            phoneNumber = request.phoneNumber,
            addressLine = request.addressLine,
            city = request.city,
            province = request.province,
            postalCode = request.postalCode
        )
        return AddressResponse.from(addressRepository.save(updated))
    }

    override fun delete(userId: UUID, addressId: UUID) {
        val address = addressRepository.findById(addressId)
            .orElseThrow { NoSuchElementException("Address not found") }

        if (address.user.userId != userId) {
            throw IllegalArgumentException("Address does not belong to user")
        }
        addressRepository.delete(address)
    }

    override fun getById(userId: UUID, addressId: UUID): AddressResponse {
        val address = addressRepository.findById(addressId)
            .orElseThrow { NoSuchElementException("Address not found") }

        if (address.user.userId != userId) {
            throw IllegalArgumentException("Address does not belong to user")
        }
        return AddressResponse.from(address)
    }

    override fun getAllByUser(userId: UUID): List<AddressResponse> =
        addressRepository.findByUserUserId(userId).map { AddressResponse.from(it) }

    @Transactional
    override fun setDefault(userId: UUID, addressId: UUID): AddressResponse {
        val address = addressRepository.findById(addressId)
            .orElseThrow { NoSuchElementException("Address not found") }

        if (address.user.userId != userId) {
            throw IllegalArgumentException("Address does not belong to user")
        }

        addressRepository.clearDefaultAddress(userId)
        address.isDefault = true
        return AddressResponse.from(addressRepository.save(address))
    }
}

