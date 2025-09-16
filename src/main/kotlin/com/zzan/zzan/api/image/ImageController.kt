package com.zzan.zzan.api.image

import com.zzan.zzan.api.image.dto.PreSignedUrlRequest
import com.zzan.zzan.api.image.dto.PreSignedUrlResponse
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.common.service.S3Service
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/images")
class ImageController(
    private val s3Service: S3Service
) {

    /**
     * 이미지 업로드용 PreSigned URL 생성
     */
    @PostMapping("/upload-url")
    fun generateUploadUrl(
        @Valid @RequestBody request: PreSignedUrlRequest
    ): ApiResponse<PreSignedUrlResponse> {

        // 파일 확장자 유효성 검사
        validateFileExtension(request.fileExtension)

        // Content-Type 결정
        val contentType = getContentType(request.fileExtension)

        val s3Response = s3Service.generateUploadPreSignedUrl(
            contentType = contentType,
            fileExtension = request.fileExtension
        )

        val response = PreSignedUrlResponse(
            uploadUrl = s3Response.preSignedUrl,
            imageUrl = s3Response.fullPath,
            fileName = s3Response.fileName,
            expiresAt = s3Response.expiresAt
        )

        return ApiResponse.ok(response)
    }

    /**
     * 이미지 다운로드용 PreSigned URL 생성
     */
    @GetMapping("/download-url")
    fun generateDownloadUrl(
        @RequestParam fileName: String,
        @RequestParam(defaultValue = "60") expirationMinutes: Long
    ): ApiResponse<String> {
        val key = "images/$fileName"

        // 파일 존재 여부 확인
        if (!s3Service.doesObjectExist(key)) {
            return ApiResponse.error("파일을 찾을 수 없습니다.")
        }

        val downloadUrl = s3Service.generateDownloadPreSignedUrl(key, expirationMinutes)

        return ApiResponse.ok(downloadUrl)
    }

    /**
     * 이미지 삭제
     */
    @DeleteMapping("/{fileName}")
    fun deleteImage(@PathVariable fileName: String): ApiResponse<String> {
        val key = "images/$fileName"

        val deleted = s3Service.deleteObject(key)

        return if (deleted) {
            ApiResponse.ok("이미지가 성공적으로 삭제되었습니다.")
        } else {
            ApiResponse.error("이미지 삭제에 실패했습니다.")
        }
    }

    private fun validateFileExtension(extension: String) {
        val allowedExtensions = setOf("jpg", "jpeg", "png", "gif", "webp")
        if (extension.lowercase() !in allowedExtensions) {
            throw IllegalArgumentException("지원하지 않는 파일 형식입니다. 허용된 형식: ${allowedExtensions.joinToString(", ")}")
        }
    }

    private fun getContentType(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }
}
