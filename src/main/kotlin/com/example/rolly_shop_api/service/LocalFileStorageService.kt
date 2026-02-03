package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.config.FileStorageProperties
import jakarta.annotation.PostConstruct
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class LocalFileStorageService(
    private val fileStorageProperties: FileStorageProperties
) {
    private lateinit var fileStorageLocation: Path

    @PostConstruct
    fun init() {
        fileStorageLocation = Paths.get(fileStorageProperties.uploadDir).toAbsolutePath().normalize()
        try {
            Files.createDirectories(fileStorageLocation)
        } catch (ex: IOException) {
            throw RuntimeException("Could not create upload directory: ${fileStorageLocation}", ex)
        }
    }

    /**
     * Store a file and return the generated filename
     */
    fun storeFile(file: MultipartFile, customFileName: String? = null): String {
        // Generate unique filename
        val originalFileName = file.originalFilename ?: "file"
        val extension = originalFileName.substringAfterLast(".", "")
        val fileName = if (customFileName != null) {
            if (extension.isNotEmpty()) "$customFileName.$extension" else customFileName
        } else {
            "${UUID.randomUUID()}.$extension"
        }

        try {
            // Check for invalid characters
            if (fileName.contains("..")) {
                throw IllegalArgumentException("Filename contains invalid path sequence: $fileName")
            }

            // Copy file to target location
            val targetLocation = fileStorageLocation.resolve(fileName)
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

            return fileName
        } catch (ex: IOException) {
            throw RuntimeException("Could not store file $fileName", ex)
        }
    }

    /**
     * Store file from byte array (e.g., from remove.bg processing)
     */
    fun storeFile(bytes: ByteArray, fileName: String): String {
        try {
            if (fileName.contains("..")) {
                throw IllegalArgumentException("Filename contains invalid path sequence: $fileName")
            }

            val targetLocation = fileStorageLocation.resolve(fileName)
            Files.write(targetLocation, bytes)

            return fileName
        } catch (ex: IOException) {
            throw RuntimeException("Could not store file $fileName", ex)
        }
    }

    /**
     * Load file as Resource
     */
    fun loadFileAsResource(fileName: String): Resource {
        try {
            val filePath = fileStorageLocation.resolve(fileName).normalize()
            val resource = UrlResource(filePath.toUri())

            if (resource.exists()) {
                return resource
            } else {
                throw NoSuchElementException("File not found: $fileName")
            }
        } catch (ex: Exception) {
            throw NoSuchElementException("File not found: $fileName")
        }
    }

    /**
     * Delete a file
     */
    fun deleteFile(fileName: String): Boolean {
        return try {
            val filePath = fileStorageLocation.resolve(fileName).normalize()
            Files.deleteIfExists(filePath)
        } catch (ex: IOException) {
            false
        }
    }

    /**
     * Check if file exists
     */
    fun fileExists(fileName: String): Boolean {
        val filePath = fileStorageLocation.resolve(fileName).normalize()
        return Files.exists(filePath)
    }
}
