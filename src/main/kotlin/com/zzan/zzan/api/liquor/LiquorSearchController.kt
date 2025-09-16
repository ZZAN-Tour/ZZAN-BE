// src/main/kotlin/com/zzan/zzan/api/liquor/LiquorSearchController.kt (로깅 추가)
package com.zzan.zzan.api.liquor

import com.zzan.zzan.api.liquor.dto.LiquorSearchResponse
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.liquor.query.handler.LiquorSearchService
import mu.KLogging
import org.springframework.web.bind.annotation.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

@RestController
@RequestMapping("/api/liquors")
class LiquorSearchController(
    private val liquorSearchService: LiquorSearchService
) {

    companion object : KLogging()

    /**
     * 자동완성용 전통주 검색
     * GET /api/liquors/autocomplete?q=참&limit=5
     */
    @GetMapping("/autocomplete")
    fun autocomplete(
        @RequestParam("q") keyword: String,
        @RequestParam(defaultValue = "10") @Min(1) @Max(20) limit: Int
    ): ApiResponse<List<LiquorSearchResponse>> {

        logger.info("🎯 [CONTROLLER] autocomplete 호출됨")
        logger.info("    요청 파라미터: keyword='$keyword', limit=$limit")

        try {
            val results = liquorSearchService.autocomplete(keyword, limit)

            logger.info("📤 [CONTROLLER] Service에서 받은 결과: ${results.size}개")
            results.forEachIndexed { index, result ->
                logger.info("    [$index] ${result.name} | 평점: ${result.averageScore} | 개수: ${result.ratingCount}")
            }

            val response = ApiResponse.ok(results)
            logger.info("✅ [CONTROLLER] 최종 응답 생성 완료: success=${response.success}, dataSize=${results.size}")

            return response

        } catch (e: Exception) {
            logger.error("💥 [CONTROLLER] autocomplete 오류", e)
            throw e
        }
    }

    /**
     * 전체 전통주 검색
     * GET /api/liquors/search?q=참이슬
     */
    @GetMapping("/search")
    fun search(
        @RequestParam("q") keyword: String
    ): ApiResponse<List<LiquorSearchResponse>> {

        logger.info("🎯 [CONTROLLER] search 호출됨: keyword='$keyword'")

        val results = liquorSearchService.search(keyword)

        logger.info("📤 [CONTROLLER] search 결과: ${results.size}개")

        return ApiResponse.ok(results)
    }
}
