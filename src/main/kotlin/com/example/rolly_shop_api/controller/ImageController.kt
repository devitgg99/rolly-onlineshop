package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.response.BaseResponse
import com.example.rolly_shop_api.model.dto.response.ImageResponse
import com.example.rolly_shop_api.service.ImageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.Base64

@RestController
@RequestMapping("/api/v1/images")
@SecurityRequirements
@Tag(name = "Image Processing", description = "Background removal and image processing")
class ImageController(
    private val imageService: ImageService
) {

    @PostMapping("/remove-background", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Remove background and upload to S3")
    fun removeBackground(
        @RequestParam("image") image: MultipartFile,
        @RequestParam("fileName", required = false) fileName: String?
    ): BaseResponse<ImageResponse> {
        val result = imageService.removeBackground(image)
        val base64 = Base64.getEncoder().encodeToString(result)
        val url = imageService.saveToS3(base64, fileName)
        return BaseResponse.success(
            data = ImageResponse(url = url),
            message = "Background removed and saved to S3"
        )
    }
}
