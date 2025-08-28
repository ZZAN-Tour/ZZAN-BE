
// src/main/kotlin/com/zzan/zzan/api/liquor/LiquorSearchController.kt (새 파일)
package com.zzan.zzan.api.liquor

import com.zzan.zzan.api.liquor.dto.LiquorSearchResponse
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.liquor.query.handler.LiquorSearchService
import org.springframework.web.bind.annotation.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

@RestController
@RequestMapping("/api/liquors")
class LiquorSearchController(
    private val liquorSearchService: LiquorSearchService
) {

    /**
     * 자동완성용 전통주 검색
     * GET /api/liquors/autocomplete?q=참&limit=5
     */
    @GetMapping("/autocomplete")
    fun autocomplete(
        @RequestParam("q") keyword: String,
        @RequestParam(defaultValue = "10") @Min(1) @Max(20) limit: Int
    ): ApiResponse<List<LiquorSearchResponse>> {
        val results = liquorSearchService.autocomplete(keyword, limit)
        return ApiResponse.ok(results)
    }

    /**
     * 전체 전통주 검색
     * GET /api/liquors/search?q=참이슬
     */
    @GetMapping("/search")
    fun search(
        @RequestParam("q") keyword: String
    ): ApiResponse<List<LiquorSearchResponse>> {
        val results = liquorSearchService.search(keyword)
        return ApiResponse.ok(results)
    }
}
