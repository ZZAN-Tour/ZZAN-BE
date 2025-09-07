package com.zzan.zzan.api.common.dto

data class CursorPageResponse<T>(
    val items: List<T>,
    val nextCursor: String? = null,
    val hasNext: Boolean = false
)
