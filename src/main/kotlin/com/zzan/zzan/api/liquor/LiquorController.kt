package com.zzan.zzan.api.liquor

import com.zzan.zzan.api.feed.dto.CursorPageRequest
import com.zzan.zzan.api.feed.dto.CursorPageResponse
import com.zzan.zzan.api.feed.dto.FeedSummaryResponse
import com.zzan.zzan.api.liquor.dto.LiquorDetailResponse
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.feed.query.FeedQueryService
import com.zzan.zzan.liquor.query.handler.LiquorQueryService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/liquors")
class LiquorController(
    private val liquorQueryService: LiquorQueryService,
    private val feedQueryService: FeedQueryService
) {

    /**
     * 기존 전통주 기본 정보 조회
     */
    @GetMapping("/{id}")
    fun getLiquorDetailById(@PathVariable id: String): ApiResponse<LiquorDetailResponse> {
        val userId = SecurityContextHolder.getContext().authentication.name
        return ApiResponse.ok(liquorQueryService.getLiquorById(id, userId))
    }

    /**
     * 특정 전통주가 태그된 피드 목록 조회 (커서 기반)
     *
     * @param id 전통주 ID
     * @param limit 한 번에 가져올 개수 (기본: 20, 최대: 50)
     * @param cursor 커서 (다음 페이지용)
     * @param sortBy 정렬 방식 (recent, score, popular)
     *
     * 예시: GET /api/liquors/liquor123/feeds?limit=20&cursor=abc123&sortBy=recent
     */
    @GetMapping("/{id}/feeds")
    fun getFeedsByLiquorTag(
        @PathVariable id: String,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "recent") sortBy: String
    ): ApiResponse<CursorPageResponse<FeedSummaryResponse>> {

        val pageRequest = CursorPageRequest(
            limit = limit.coerceIn(1, 50), // 1~50 사이로 제한
            cursor = cursor,
            sortBy = sortBy
        )

        val feeds = feedQueryService.getFeedsByLiquorTagWithCursor(id, pageRequest)
        return ApiResponse.ok(feeds)
    }

}
