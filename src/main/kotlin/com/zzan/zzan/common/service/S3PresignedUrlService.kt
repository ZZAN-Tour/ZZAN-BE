// S3PresignedUrlService.kt
package com.zzan.zzan.common.service

import com.zzan.zzan.api.feed.dto.ImageUploadInfo
import com.zzan.zzan.api.feed.dto.PresignedUrlRequest
import com.zzan.zzan.api.feed.dto.PresignedUrlResponse
import com.zzan.zzan.common.exception.CustomException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.*

@Service
class S3PresignedUrlService(
    private val s3Presigner: S3Presigner,
    @Value("\${aws.s3.bucket.name}") private val bucketName: String,
    @Value("\${aws.s3.region}") private val region: String
) {

    companion object {
        private const val MAX_IMAGE_COUNT = 10
        private const val PRESIGNED_URL_DURATION_MINUTES = 10L
        private val ALLOWED_IMAGE_TYPES = setOf("jpg", "jpeg", "png", "webp")
    }

    fun generatePresignedUrls(userId: String, request: PresignedUrlRequest): PresignedUrlResponse {
        validateRequest(request)

        val uploadInfos = request.imageTypes.mapIndexed { index, imageType ->
            val key = generateS3Key(userId, imageType)
            val presignedUrl = generatePresignedUrl(key, imageType)
            val imageUrl = "https://$bucketName.s3.$region.amazonaws.com/$key"

            ImageUploadInfo(
                uploadUrl = presignedUrl,
                imageUrl = imageUrl,
                key = key,
                index = index
            )
        }

        return PresignedUrlResponse(uploadInfos = uploadInfos)
    }

    private fun validateRequest(request: PresignedUrlRequest) {
        if (request.imageCount > MAX_IMAGE_COUNT) {
            throw CustomException(
                status = HttpStatus.BAD_REQUEST,
                message = "최대 ${MAX_IMAGE_COUNT}개의 이미지만 업로드 가능합니다."
            )
        }

        if (request.imageTypes.size != request.imageCount) {
            throw CustomException(
                status = HttpStatus.BAD_REQUEST,
                message = "이미지 개수와 타입 개수가 일치하지 않습니다."
            )
        }

        request.imageTypes.forEach { imageType ->
            if (!ALLOWED_IMAGE_TYPES.contains(imageType.lowercase())) {
                throw CustomException(
                    status = HttpStatus.BAD_REQUEST,
                    message = "지원하지 않는 이미지 형식입니다: $imageType"
                )
            }
        }
    }

    private fun generateS3Key(userId: String, imageType: String): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return "feeds/$userId/$timestamp-$uuid.$imageType"
    }

    private fun generatePresignedUrl(key: String, imageType: String): String {
        val contentType = when (imageType.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/*"
        }

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(PRESIGNED_URL_DURATION_MINUTES))
            .putObjectRequest(putObjectRequest)
            .build()

        return s3Presigner.presignPutObject(presignRequest).url().toString()
    }
}