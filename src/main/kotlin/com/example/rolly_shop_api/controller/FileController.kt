package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.response.BaseResponse
import com.example.rolly_shop_api.service.LocalFileStorageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "File Storage", description = "Local file upload and download endpoints")
class FileController(
    private val localFileStorageService: LocalFileStorageService
) {

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Upload a file",
        description = "🔒 ADMIN ONLY - Upload a file to local storage. Returns the public URL."
    )
    fun uploadFile(
        @Parameter(description = "File to upload", content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)])
        @RequestPart("file") file: MultipartFile,
        @Parameter(description = "Custom filename (optional)")
        @RequestParam(required = false) fileName: String?
    ): BaseResponse<FileUploadResponse> {
        val storedFileName = localFileStorageService.storeFile(file, fileName)
        
        val fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/v1/files/")
            .path(storedFileName)
            .toUriString()

        return BaseResponse.success(
            FileUploadResponse(
                fileName = storedFileName,
                url = fileUrl,
                size = file.size,
                contentType = file.contentType ?: "application/octet-stream"
            ),
            "File uploaded successfully"
        )
    }

    @PostMapping("/upload-multiple", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Upload multiple files",
        description = "🔒 ADMIN ONLY - Upload multiple files at once. Returns list of public URLs."
    )
    fun uploadMultipleFiles(
        @Parameter(description = "Files to upload")
        @RequestPart("files") files: List<MultipartFile>
    ): BaseResponse<List<FileUploadResponse>> {
        val responses = files.map { file ->
            val storedFileName = localFileStorageService.storeFile(file)
            
            val fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/files/")
                .path(storedFileName)
                .toUriString()

            FileUploadResponse(
                fileName = storedFileName,
                url = fileUrl,
                size = file.size,
                contentType = file.contentType ?: "application/octet-stream"
            )
        }

        return BaseResponse.success(responses, "${files.size} files uploaded successfully")
    }

    @GetMapping("/{fileName:.+}")
    @SecurityRequirements
    @Operation(
        summary = "Get file (public)",
        description = "🌐 PUBLIC - Download/view a file by filename. Use this URL for images."
    )
    fun getFile(@PathVariable fileName: String): ResponseEntity<Resource> {
        val resource = localFileStorageService.loadFileAsResource(fileName)
        
        // Determine content type
        val contentType = when {
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> MediaType.IMAGE_JPEG
            fileName.endsWith(".png", true) -> MediaType.IMAGE_PNG
            fileName.endsWith(".gif", true) -> MediaType.IMAGE_GIF
            fileName.endsWith(".webp", true) -> MediaType.parseMediaType("image/webp")
            fileName.endsWith(".svg", true) -> MediaType.parseMediaType("image/svg+xml")
            fileName.endsWith(".pdf", true) -> MediaType.APPLICATION_PDF
            else -> MediaType.APPLICATION_OCTET_STREAM
        }

        return ResponseEntity.ok()
            .contentType(contentType)
            .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000") // Cache for 1 year
            .body(resource)
    }

    @DeleteMapping("/{fileName:.+}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete a file",
        description = "🔒 ADMIN ONLY - Delete a file from storage."
    )
    fun deleteFile(@PathVariable fileName: String): BaseResponse<Unit> {
        val deleted = localFileStorageService.deleteFile(fileName)
        return if (deleted) {
            BaseResponse.ok("File deleted successfully")
        } else {
            BaseResponse.error("File not found or could not be deleted")
        }
    }
}

data class FileUploadResponse(
    val fileName: String,
    val url: String,
    val size: Long,
    val contentType: String
)
