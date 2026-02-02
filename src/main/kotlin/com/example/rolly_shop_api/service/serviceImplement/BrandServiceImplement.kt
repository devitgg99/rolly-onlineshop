package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.model.dto.request.BrandRequest
import com.example.rolly_shop_api.model.dto.response.BrandResponse
import com.example.rolly_shop_api.model.entity.Brand
import com.example.rolly_shop_api.repository.BrandRepository
import com.example.rolly_shop_api.service.BrandService
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class BrandServiceImplement(
    private val brandRepository: BrandRepository
) : BrandService {

    override fun create(request: BrandRequest): BrandResponse {
        if (brandRepository.existsByName(request.name)) {
            throw IllegalArgumentException("Brand with name '${request.name}' already exists")
        }
        val brand = Brand(
            name = request.name,
            logoUrl = request.logoUrl,
            description = request.description
        )
        return BrandResponse.from(brandRepository.save(brand))
    }

    override fun update(id: UUID, request: BrandRequest): BrandResponse {
        val brand = brandRepository.findById(id)
            .orElseThrow { NoSuchElementException("Brand not found") }
        val updated = brand.copy(
            name = request.name,
            logoUrl = request.logoUrl,
            description = request.description,
            updatedAt = Instant.now()
        )
        return BrandResponse.from(brandRepository.save(updated))
    }

    override fun delete(id: UUID) {
        if (!brandRepository.existsById(id)) {
            throw NoSuchElementException("Brand not found")
        }
        brandRepository.deleteById(id)
    }

    override fun getById(id: UUID): BrandResponse {
        val brand = brandRepository.findById(id)
            .orElseThrow { NoSuchElementException("Brand not found") }
        return BrandResponse.from(brand)
    }

    override fun getAll(): List<BrandResponse> =
        brandRepository.findAll().map { BrandResponse.from(it) }
}
