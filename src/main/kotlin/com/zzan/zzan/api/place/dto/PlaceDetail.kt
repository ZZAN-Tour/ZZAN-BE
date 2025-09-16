package com.zzan.zzan.api.place.dto

data class PlaceDetail(
    val id: String,
    val name: String,
    val address: String,
    val phone: String?,
    val longitude: Double,
    val latitude: Double,
    val score: Double?,
    val count: Int,
)
