
// 1. 추가 DTO 클래스
package com.zzan.zzan.api.feed.dto

import java.time.LocalDateTime

/**
 * 이미지 정보 없는 피드 상세 응답 (중간 단계)
 */
data class FeedDetailResponseWithoutImages(
    val id: String,
    val userId: String,
    val userNickname: String,
    val userProfileImageUrl: String?,
    val imageUrl: String,
    val score: Double?,
    val text: String?,
    val place: PlaceInfo,
    val buyPlace: PlaceInfo?,
    val createdAt: LocalDateTime
)
