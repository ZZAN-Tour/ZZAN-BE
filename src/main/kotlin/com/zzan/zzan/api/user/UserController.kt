package com.zzan.zzan.api.user

import com.zzan.zzan.api.common.dto.CursorPageRequest
import com.zzan.zzan.api.common.dto.CursorPageResponse
import com.zzan.zzan.api.user.dto.FeedScrapResponse
import com.zzan.zzan.api.user.dto.LiquorScrapResponse
import com.zzan.zzan.api.user.dto.ScrapResponse
import com.zzan.zzan.api.user.dto.UserFeedResponse
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.user.command.application.UserCommandService
import com.zzan.zzan.user.query.handler.UserQueryService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(
    private val userQueryService: UserQueryService,
    private val userCommandService: UserCommandService
) {
    @GetMapping("/feed")
    fun getMyFeed(request: CursorPageRequest): ApiResponse<CursorPageResponse<UserFeedResponse>> {
        val userId = SecurityContextHolder.getContext().authentication.name
        return ApiResponse.ok(userQueryService.getMyFeed(request, userId))
    }

    @GetMapping("/scraps/feed")
    fun getFeedScraps(request: CursorPageRequest): ApiResponse<CursorPageResponse<FeedScrapResponse>> {
        val userId = SecurityContextHolder.getContext().authentication.name
        return ApiResponse.ok(userQueryService.getFeedScraps(request, userId))
    }

    @GetMapping("/scraps/liquor")
    fun getLiquorScraps(request: CursorPageRequest): ApiResponse<CursorPageResponse<LiquorScrapResponse>> {
        val userId = SecurityContextHolder.getContext().authentication.name
        return ApiResponse.ok(userQueryService.getLiquorScraps(request, userId))
    }


    @PostMapping("/scraps/feed")
    fun createFeedScrap(@RequestParam feedId: String): ApiResponse<ScrapResponse> {
        val userId = SecurityContextHolder.getContext().authentication.name
        return ApiResponse.ok(userCommandService.createFeedScrap(userId, feedId))
    }

    @PostMapping("/scraps/liquor")
    fun createLiquorScrap(@RequestParam liquorId: String): ApiResponse<ScrapResponse> {
        val userId = SecurityContextHolder.getContext().authentication.name
        return ApiResponse.ok(userCommandService.createLiquorScrap(userId, liquorId))
    }

    @DeleteMapping("/scraps/feed")
    fun deleteFeedScrap(@RequestParam feedId: String): ApiResponse<ScrapResponse> {
        val userId = SecurityContextHolder.getContext().authentication.name
        return ApiResponse.ok(userCommandService.deleteFeedScrap(userId, feedId))
    }

    @DeleteMapping("/scraps/liquor")
    fun deleteLiquorScrap(@RequestParam liquorId: String): ApiResponse<ScrapResponse> {
        val userId = SecurityContextHolder.getContext().authentication.name
        return ApiResponse.ok(userCommandService.deleteLiquorScrap(userId, liquorId))
    }
}
