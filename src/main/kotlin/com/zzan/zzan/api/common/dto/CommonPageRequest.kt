package com.zzan.zzan.api.common.dto

import org.hibernate.validator.constraints.Range

data class CommonPageRequest(
    @field:Range(min = 1, max = 100, message = "size는 1 이상 20 이하이어야 합니다.")
    val size: Int = 10,
    val cursor: String? = null
)
