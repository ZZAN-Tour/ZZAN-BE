package com.zzan.zzan.place.command.domain.vo

data class Longitude(val value: Double) {
    init {
        require(value in -180.0..180.0) { "경도는 -180도에서 180도 사이의 값이어야 합니다: $value" }
    }
}
