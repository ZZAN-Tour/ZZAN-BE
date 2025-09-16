package com.zzan.zzan.api.liquorrating.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size

data class UpdateLiquorRatingRequest(
    @field:DecimalMin(value = "1.0", message = "평점은 1.0 이상이어야 합니다.")
    @field:DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
    val score: Double,

    @field:Size(max = 500, message = "코멘트는 500자 이하여야 합니다.")
    val comment: String? = null
)
