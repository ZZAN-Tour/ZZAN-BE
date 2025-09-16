package com.zzan.zzan.place.query.handler

import com.zzan.zzan.api.place.dto.PlaceDetail
import com.zzan.zzan.api.place.dto.PlaceResponse
import com.zzan.zzan.place.command.domain.vo.ViewBox
import com.zzan.zzan.place.query.repository.PlaceQueryRepository
import org.springframework.stereotype.Service


@Service
class PlaceQueryServiceImpl(
    private val placeQueryRepository: PlaceQueryRepository
) : PlaceQueryService {

    override fun getPlacesInViewBox(viewBox: ViewBox): List<PlaceResponse> {
        return placeQueryRepository.findPlacesByViewBox(viewBox)
    }

    override fun getPlaceDetailById(placeId: String): PlaceDetail {
        return placeQueryRepository.getPlaceDetailsById(placeId)
    }
}
