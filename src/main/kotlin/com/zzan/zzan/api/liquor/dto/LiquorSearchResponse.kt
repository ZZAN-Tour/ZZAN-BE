// src/main/kotlin/com/zzan/zzan/api/liquor/dto/LiquorSearchResponse.kt (수정)
package com.zzan.zzan.api.liquor.dto

data class LiquorSearchResponse(
    val id: String,
    val name: String,
    val type: String?,
    val brewery: String?,
    val imageUrl: String?
)
