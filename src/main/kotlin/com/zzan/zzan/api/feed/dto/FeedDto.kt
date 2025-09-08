package com.zzan.zzan.api.feed.dto

import com.zzan.zzan.api.liquortag.dto.TagInFeedRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class CreateFeedRequest(
    @field:NotBlank(message = "사용자 ID는 필수입니다.")
    val userId: String,

    @field:NotBlank(message = "대표 이미지 URL은 필수입니다.")
    val imageUrl: String,

    @field:DecimalMin(value = "0.0", message = "평점은 0.0 이상이어야 합니다.")
    @field:DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
    val score: Double?,

    @field:Size(max = 2000, message = "피드 내용은 2000자 이하여야 합니다.")
    val text: String?,

    @field:NotBlank(message = "장소 ID는 필수입니다.")
    val placeId: String,

    val buyPlaceId: String?,

    @field:Valid
    @field:NotEmpty(message = "최소 1개의 이미지가 필요합니다.")
    val images: List<FeedImageRequest>,

    @field:Valid
    val tags: List<TagInFeedRequest> = emptyList() // 태그 추가
)



data class FeedImageRequest(
    @field:NotBlank(message = "이미지 URL은 필수입니다.")
    val imageUrl: String,

    @field:Min(value = 1, message = "이미지 순서는 1 이상이어야 합니다.")
    val orderNum: Int
)

data class UpdateFeedRequest(
    @field:DecimalMin(value = "0.0", message = "평점은 0.0 이상이어야 합니다.")
    @field:DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
    val score: Double?,

    @field:Size(max = 2000, message = "피드 내용은 2000자 이하여야 합니다.")
    val text: String?,

    val buyPlaceId: String?
)

data class FeedDetailResponse(
    val id: String,
    val userId: String,
    val userNickname: String,
    val userProfileImageUrl: String?,
    val imageUrl: String,
    val score: Double?,
    val text: String?,
    val place: PlaceInfo,
    val buyPlace: PlaceInfo?,
    val images: List<FeedImageInfo>,
    val tags: List<TagInfo>,
    val createdAt: LocalDateTime
)

// 태그 정보 DTO
data class TagInfo(
    val id: String,
    val imageId: String,
    val liquorId: String,
    val liquorName: String,
    val liquorType: String,
    val liquorBrewery: String?,
    val tagX: Double,
    val tagY: Double,

)

data class FeedSummaryResponse(
    val id: String,
    val userId: String,
    val userNickname: String,
    val userProfileImageUrl: String?,
    val imageUrl: String,
    val score: Double?,
    val text: String?,
    val placeName: String,
    val createdAt: LocalDateTime,

)

data class FeedSummaryCountInfo(
    val likes: Long = 0,
    val scraps: Long = 0,
    val taggedLiquors: Int = 0
)

data class PlaceInfo(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

data class FeedImageInfo(
    val id: String,
    val imageUrl: String,
    val orderNum: Int
)

data class FeedSearchCriteria(
    val userId: String? = null,
    val placeId: String? = null,
    val minScore: Double? = null,
    val maxScore: Double? = null,
    val fromDate: LocalDateTime? = null,
    val toDate: LocalDateTime? = null
)

data class PageRequest(
    @field:Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    val page: Int = 0,

    @field:Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @field:Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
    val size: Int = 20,

    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
)

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
