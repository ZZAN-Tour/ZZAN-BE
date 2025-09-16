package com.zzan.zzan.api.image.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDateTime

data class PreSignedUrlRequest(
    @field:NotBlank(message = "파일 확장자는 필수입니다.")
    @field:Pattern(regexp = "^(jpg|jpeg|png|gif|webp)$", message = "지원하지 않는 파일 형식입니다.")
    val fileExtension: String
)

data class PreSignedUrlResponse(
    val uploadUrl: String,       // 업로드용 PreSigned URL
    val imageUrl: String,        // 업로드 완료 후 접근할 이미지 URL
    val fileName: String,        // 생성된 파일명
    val expiresAt: LocalDateTime // URL 만료 시간
)
