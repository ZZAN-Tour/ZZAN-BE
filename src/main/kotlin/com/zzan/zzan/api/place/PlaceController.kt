package com.zzan.zzan.api.place

import com.zzan.zzan.api.place.dto.PlaceDetail
import com.zzan.zzan.api.place.dto.PlaceResponse
import com.zzan.zzan.api.place.dto.ViewBoxRequest
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.place.query.handler.PlaceQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/place")
class PlaceController(
    private val placeQueryService: PlaceQueryService
) {
    @GetMapping("/list")
    fun getPlaces(request: ViewBoxRequest): ApiResponse<List<PlaceResponse>> {
        val viewBox = request.toViewBox()
        return ApiResponse.ok(placeQueryService.getPlacesInViewBox(viewBox))
    }

    @GetMapping("/detail")
    fun getPlaceDetailById(placeId: String): ApiResponse<PlaceDetail> {
        return ApiResponse.ok(placeQueryService.getPlaceDetailById(placeId))
    }
}
