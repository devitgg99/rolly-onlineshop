package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.UserLogin
import com.example.rolly_shop_api.model.dto.request.UserRequest
import com.example.rolly_shop_api.model.dto.response.BaseResponse
import com.example.rolly_shop_api.service.AuthService
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
@SecurityRequirements
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    fun register(@RequestBody userRequest: UserRequest): BaseResponse<Unit> {
        authService.register(userRequest)
        return BaseResponse.ok("User registered successfully")
    }

    @PostMapping("/login")
    fun login(@RequestBody userRequest: UserLogin): BaseResponse<String> =
        BaseResponse.success(
            data = authService.login(userRequest),
            message = "Login successful"
        )
}