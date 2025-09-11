package com.zzan.zzan.api.common.dto

data class CursorPageRequest(
    val size: Int = 10,
    val cursor: String? = null
)
