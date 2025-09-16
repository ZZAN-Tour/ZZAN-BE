package com.zzan.zzan.place.command.domain.vo

data class ViewBox(
    val minLongitude: Longitude,
    val maxLongitude: Longitude,
    val minLatitude: Latitude,
    val maxLatitude: Latitude
) {
    init {
        require(minLongitude.value <= maxLongitude.value) {
            "최소 경도는 최대 경도보다 작거나 같아야 합니다: ${minLongitude.value} <= ${maxLongitude.value}"
        }
        require(minLatitude.value <= maxLatitude.value) {
            "최소 위도는 최대 위도보다 작거나 같아야 합니다: ${minLatitude.value} <= ${maxLatitude.value}"
        }
    }
}
