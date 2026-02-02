package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.*

interface AddressRepository : JpaRepository<Address, UUID> {
    fun findByUserUserId(userId: UUID): List<Address>
    fun findByUserUserIdAndIsDefaultTrue(userId: UUID): Address?

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.userId = :userId")
    fun clearDefaultAddress(userId: UUID)
}

