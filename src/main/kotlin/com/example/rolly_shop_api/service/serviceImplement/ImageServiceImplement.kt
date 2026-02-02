package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.config.RemoveBgProperties
import com.example.rolly_shop_api.service.ImageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

@Service
class ImageServiceImplement(
    private val removeBgProperties: RemoveBgProperties,
    private val restTemplate: RestTemplate,
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucketName: String,
    @Value("\${cloud.aws.region.static}") private val region: String
) : ImageService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun removeBackground(image: MultipartFile): ByteArray {
        log.info("Removing background from image: ${image.originalFilename}, size: ${image.size} bytes")

        val headers = HttpHeaders().apply {
            set("X-Api-Key", removeBgProperties.apiKey)
            contentType = MediaType.MULTIPART_FORM_DATA
        }

        val fileResource = object : ByteArrayResource(image.bytes) {
            override fun getFilename() = image.originalFilename ?: "image.png"
        }

        val body = LinkedMultiValueMap<String, Any>().apply {
            add("image_file", fileResource)
            add("size", "auto")
        }

        val request = HttpEntity(body, headers)

        try {
            val response = restTemplate.postForEntity(
                removeBgProperties.apiUrl,
                request,
                ByteArray::class.java
            )
            log.info("Remove.bg response status: ${response.statusCode}")
            return response.body ?: throw RuntimeException("Empty response from remove.bg")
        } catch (e: HttpClientErrorException) {
            log.error("Remove.bg API error: ${e.statusCode} - ${e.responseBodyAsString}")
            throw RuntimeException("Remove.bg API error: ${e.responseBodyAsString}")
        }
    }

    override fun removeBackgroundFromUrl(imageUrl: String): ByteArray {
        val headers = HttpHeaders().apply {
            set("X-Api-Key", removeBgProperties.apiKey)
            contentType = MediaType.MULTIPART_FORM_DATA
        }

        val body = LinkedMultiValueMap<String, Any>().apply {
            add("image_url", imageUrl)
            add("size", "auto")
        }

        val request = HttpEntity(body, headers)
        val response = restTemplate.postForEntity(
            removeBgProperties.apiUrl,
            request,
            ByteArray::class.java
        )

        return response.body ?: throw RuntimeException("Failed to remove background")
    }

    override fun saveToS3(base64: String, fileName: String?): String {
        val imageBytes = Base64.getDecoder().decode(base64)
        val key = "images/${fileName ?: "img-${UUID.randomUUID()}"}.png"

        val putRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType("image/png")
            .build()

        s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes))

        return "https://${bucketName}.s3.${region}.amazonaws.com/${key}"
    }
}

