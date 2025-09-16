package com.zzan.zzan.api.place.dto

data class PlaceResponse(
    val id: String,
    val name: String,
    val count: Int,
    val score: Double?,
    val address: String,
    val phone: String?,
    val longitude: Double,
    val latitude: Double
)
