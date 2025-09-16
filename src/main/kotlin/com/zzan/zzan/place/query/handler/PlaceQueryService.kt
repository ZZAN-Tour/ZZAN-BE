package com.zzan.zzan.place.query.handler

import com.zzan.zzan.api.place.dto.PlaceDetail
import com.zzan.zzan.api.place.dto.PlaceResponse
import com.zzan.zzan.place.command.domain.vo.ViewBox

interface PlaceQueryService {
    /**
     * 주어진 ViewBox 내의 장소들을 조회합니다.
     *
     * @param viewBox 조회할 ViewBox 영역
     * @return 해당 ViewBox 내의 장소 목록
     */
    fun getPlacesInViewBox(viewBox: ViewBox): List<PlaceResponse>

    fun getPlaceDetailById(placeId: String): PlaceDetail
}
