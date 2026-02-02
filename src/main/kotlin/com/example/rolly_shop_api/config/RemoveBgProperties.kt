package com.example.rolly_shop_api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("removebg")
data class RemoveBgProperties(
    val apiKey: String,
    val apiUrl: String = "https://api.remove.bg/v1.0/removebg"
)

