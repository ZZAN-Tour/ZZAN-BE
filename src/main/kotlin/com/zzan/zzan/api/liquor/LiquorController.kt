package com.zzan.zzan.api.liquor

import com.zzan.zzan.api.liquor.dto.LiquorDetailResponse
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.liquor.query.handler.LiquorQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class LiquorController (
    private val liquorQueryService: LiquorQueryService,
){
    @GetMapping("/liquors/{id}")
    fun getLiquorById(@PathVariable id: String): ApiResponse<LiquorDetailResponse> {
        return ApiResponse.ok(
            liquorQueryService.getLiquorById(id)
        )
    }
}