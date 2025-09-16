package com.zzan.zzan.place.query.repository

import com.zzan.zzan.api.place.dto.PlaceDetail
import com.zzan.zzan.api.place.dto.PlaceResponse
import com.zzan.zzan.place.command.domain.Place
import com.zzan.zzan.place.command.domain.vo.ViewBox
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PlaceQueryRepository : JpaRepository<Place, Long> {
    @Query(
        """
            SELECT new com.zzan.zzan.api.place.dto.PlaceResponse(
                p.id, p.name, p.count, p.score, 
                p.address, p.phone, p.longitude, p.latitude
            )
            FROM Place p 
            WHERE function('ST_Within', p.location, 
                   function('ST_MakeEnvelope', 
                       :#{#viewBox.minLongitude.value}, 
                       :#{#viewBox.minLatitude.value}, 
                       :#{#viewBox.maxLongitude.value}, 
                       :#{#viewBox.maxLatitude.value}, 
                       4326)) = true
        """
    )
    fun findPlacesByViewBox(@Param("viewBox") viewBox: ViewBox): List<PlaceResponse>

    @Query(
        """
            SELECT new com.zzan.zzan.api.place.dto.PlaceDetail(
                p.id, p.name, p.address, p.phone, 
                p.longitude, p.latitude, p.score, p.count
            )
            FROM Place p 
            WHERE p.id = :id
        """
    )
    fun getPlaceDetailsById(id: String): PlaceDetail
}

