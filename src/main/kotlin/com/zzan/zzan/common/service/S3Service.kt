package com.zzan.zzan.common.service

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.github.f4b6a3.ulid.UlidCreator
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class S3Service(
    private val amazonS3: AmazonS3
) : KLogging() {

    @Value("\${cloud.aws.s3.bucket}")
    private lateinit var bucketName: String

    /**
     * 이미지 업로드용 PreSigned URL 생성
     */
    fun generateUploadPreSignedUrl(
        contentType: String,
        fileExtension: String
    ): S3PreSignedUrlResponse {
        val fileName = generateFileName(fileExtension)
        val key = "images/$fileName"

        val metadata = ObjectMetadata().apply {
            this.contentType = contentType
            addUserMetadata("uploaded-by", "zzan-app")
        }

        val expiration = Date.from(
            LocalDateTime.now().plusMinutes(15).atZone(ZoneId.systemDefault()).toInstant()
        )

        val generatePresignedUrlRequest = GeneratePresignedUrlRequest(bucketName, key)
            .withMethod(HttpMethod.PUT)
            .withExpiration(expiration)

        generatePresignedUrlRequest.putCustomRequestHeader("Content-Type", contentType)

        val preSignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest)

        logger.info("Generated upload PreSigned URL for key: $key")

        return S3PreSignedUrlResponse(
            preSignedUrl = preSignedUrl.toString(),
            fileName = fileName,
            fullPath = "https://$bucketName.s3.${amazonS3.regionName}.amazonaws.com/$key",
            expiresAt = expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }

    /**
     * 이미지 다운로드용 PreSigned URL 생성
     */
    fun generateDownloadPreSignedUrl(key: String, expirationMinutes: Long = 60): String {
        val expiration = Date.from(
            LocalDateTime.now().plusMinutes(expirationMinutes).atZone(ZoneId.systemDefault()).toInstant()
        )

        val generatePresignedUrlRequest = GeneratePresignedUrlRequest(bucketName, key)
            .withMethod(HttpMethod.GET)
            .withExpiration(expiration)

        val preSignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest)

        logger.info("Generated download PreSigned URL for key: $key")

        return preSignedUrl.toString()
    }

    /**
     * 파일 존재 여부 확인
     */
    fun doesObjectExist(key: String): Boolean {
        return try {
            amazonS3.doesObjectExist(bucketName, key)
        } catch (e: Exception) {
            logger.error("Error checking object existence for key: $key", e)
            false
        }
    }

    /**
     * 파일 삭제
     */
    fun deleteObject(key: String): Boolean {
        return try {
            amazonS3.deleteObject(bucketName, key)
            logger.info("Successfully deleted object with key: $key")
            true
        } catch (e: Exception) {
            logger.error("Error deleting object with key: $key", e)
            false
        }
    }

    private fun generateFileName(extension: String): String {
        val timestamp = System.currentTimeMillis()
        val ulid = UlidCreator.getUlid().toString().lowercase()
        return "${timestamp}_${ulid}.$extension"
    }
}

data class S3PreSignedUrlResponse(
    val preSignedUrl: String,
    val fileName: String,
    val fullPath: String,
    val expiresAt: LocalDateTime
)
