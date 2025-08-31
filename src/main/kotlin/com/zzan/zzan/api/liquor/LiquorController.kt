package com.zzan.zzan.api.liquor

import com.zzan.zzan.api.liquor.dto.LiquorDetailResponse
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.liquor.query.handler.LiquorQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/liquor")
class LiquorController(
    private val liquorQueryService: LiquorQueryService,
) {
    @GetMapping("/{id}")
    fun getLiquorById(@PathVariable id: String): ApiResponse<LiquorDetailResponse> {
        return ApiResponse.ok(liquorQueryService.getLiquorById(id))
    }
}
