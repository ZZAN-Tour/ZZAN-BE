// src/main/kotlin/com/zzan/zzan/api/liquortag/dto/LiquorTagDto.kt
package com.zzan.zzan.api.liquortag.dto

import jakarta.validation.constraints.*

data class CreateLiquorTagRequest(
    @field:NotBlank(message = "피드 ID는 필수입니다.")
    val feedId: String,

    @field:NotBlank(message = "이미지 ID는 필수입니다.")
    val imageId: String,

    @field:NotBlank(message = "전통주 ID는 필수입니다.")
    val liquorId: String,

    @field:DecimalMin(value = "0.0", message = "tagX는 0.0 이상이어야 합니다.")
    @field:DecimalMax(value = "1.0", message = "tagX는 1.0 이하여야 합니다.")
    val tagX: Double,

    @field:DecimalMin(value = "0.0", message = "tagY는 0.0 이상이어야 합니다.")
    @field:DecimalMax(value = "1.0", message = "tagY는 1.0 이하여야 합니다.")
    val tagY: Double
)

data class LiquorTagResponse(
    val id: String,
    val feedId: String,
    val imageId: String,
    val liquorId: String,
    val liquorName: String,
    val liquorType: String,
    val liquorBrewery: String?,
    val tagX: Double,
    val tagY: Double
)

data class TagInFeedRequest(
    @field:Min(value = 1, message = "이미지 순서는 1 이상이어야 합니다.")
    val imageOrderNum: Int, // 몇 번째 이미지에 태그하는지

    @field:NotBlank(message = "전통주 ID는 필수입니다.")
    val liquorId: String,

    @field:DecimalMin(value = "0.0")
    @field:DecimalMax(value = "1.0")
    val tagX: Double,

    @field:DecimalMin(value = "0.0")
    @field:DecimalMax(value = "1.0")
    val tagY: Double
)
