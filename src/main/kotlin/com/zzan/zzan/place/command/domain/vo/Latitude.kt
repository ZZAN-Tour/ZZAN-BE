package com.zzan.zzan.place.command.domain.vo

data class Latitude(val value: Double) {
    init {
        require(value in -90.0..90.0) { "위도는 -90도에서 90도 사이여야 합니다: $value" }
    }
}
