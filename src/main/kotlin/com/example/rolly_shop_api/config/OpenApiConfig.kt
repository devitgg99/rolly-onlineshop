package com.example.rolly_shop_api.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Rolly Shop API",
        description = "E-commerce API with JWT Authentication",
        version = "1.0.0"
    ),
    servers = [
        Server(url = "https://devit.tail473287.ts.net", description = "Production (Tailscale)"),
        Server(url = "http://localhost:8080", description = "Local Development")
    ],
    security = [SecurityRequirement(name = "Bearer Authentication")]
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
class OpenApiConfig

