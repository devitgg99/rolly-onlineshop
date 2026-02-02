package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.entity.User
import com.example.rolly_shop_api.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.*

@Service
class CurrentUserService(
    private val userRepository: UserRepository
) {
    fun getCurrentUser(): User {
        val username = SecurityContextHolder.getContext().authentication?.name
            ?: throw IllegalStateException("No authenticated user")
        
        return userRepository.findByEmail(username)
            ?: userRepository.findByPhoneNumber(username)
            ?: throw NoSuchElementException("User not found")
    }
    
    fun getCurrentUserId(): UUID = getCurrentUser().userId!!
}

