package com.zzan.zzan.api.place.dto

import com.zzan.zzan.place.command.domain.vo.Latitude
import com.zzan.zzan.place.command.domain.vo.Longitude
import com.zzan.zzan.place.command.domain.vo.ViewBox

data class ViewBoxRequest(
    val minLongitude: Double,
    val maxLongitude: Double,
    val minLatitude: Double,
    val maxLatitude: Double
) {
    fun toViewBox(): ViewBox {
        return ViewBox(
            minLongitude = Longitude(minLongitude),
            maxLongitude = Longitude(maxLongitude),
            minLatitude = Latitude(minLatitude),
            maxLatitude = Latitude(maxLatitude)
        )
    }
}
