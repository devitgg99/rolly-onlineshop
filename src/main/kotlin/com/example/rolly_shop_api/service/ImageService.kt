package com.example.rolly_shop_api.service

import org.springframework.web.multipart.MultipartFile

interface ImageService {
    fun removeBackground(image: MultipartFile): ByteArray
    fun removeBackgroundFromUrl(imageUrl: String): ByteArray
    fun saveToS3(base64: String, fileName: String?): String
}

