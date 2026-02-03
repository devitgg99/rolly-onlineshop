package com.example.rolly_shop_api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "file")
data class FileStorageProperties(
    val uploadDir: String = "uploads"
)
