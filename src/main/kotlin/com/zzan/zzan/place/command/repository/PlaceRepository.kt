package com.zzan.zzan.place.command.repository

import com.zzan.zzan.place.command.domain.Place
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PlaceRepository : JpaRepository<Place, String> {
    fun findByKakaoPlaceId(kakaoPlaceId: String): Place?

    @Modifying
    @Query("UPDATE Place p SET p.count = p.count + :incrementBy WHERE p.id = :placeId")
    fun incrementFeedCountById(placeId: String, incrementBy: Long = 1)
}
