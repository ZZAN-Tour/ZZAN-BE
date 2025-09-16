package com.zzan.zzan.place.command.domain

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.locationtech.jts.geom.Point

@Entity
@Table(name = "places")
class Place(
    @Id
    @Column(length = 26)
    val id: String = UlidCreator.getUlid().toString(),

    val name: String, // 장소 이름

    val count: Int = 0, // 피드 수

    val kakaoPlaceId: String, // 카카오 장소 ID

    val score: Double? = null, // 장소에서 남겨진 피드 평균 평점 (0~5)

    val address: String, // 장소 주소

    val phone: String? = null, // 전화번호

    val longitude: Double, // 경도 (X 좌표)

    val latitude: Double, // 위도 (Y 좌표)

    @Column(
        columnDefinition = "geometry(Point, 4326) GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)) STORED",
        insertable = false,
        updatable = false
    )
    val location: Point? = null, // 장소 위치 (경도, 위도)
) {
    companion object {
        fun of(
            name: String, address: String,
            phone: String? = null,
            longitude: Double,
            latitude: Double,
            kakaoPlaceId: String
        ): Place {
            return Place(
                kakaoPlaceId = kakaoPlaceId,
                name = name,
                address = address,
                phone = phone,
                longitude = longitude,
                latitude = latitude
            )
        }
    }
}
