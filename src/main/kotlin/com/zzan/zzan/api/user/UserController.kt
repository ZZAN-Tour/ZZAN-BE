package com.zzan.zzan.api.user

import com.zzan.zzan.api.common.dto.CommonPageRequest
import com.zzan.zzan.api.common.dto.CommonPageResponse
import com.zzan.zzan.api.user.dto.FeedScrapResponse
import com.zzan.zzan.api.user.dto.LiquorScrapResponse
import com.zzan.zzan.api.user.dto.ScrapResponse
import com.zzan.zzan.api.user.dto.UserFeedResponse
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.user.command.application.UserCommandService
import com.zzan.zzan.user.query.handler.UserQueryService
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(
    private val userQueryService: UserQueryService,
    private val userCommandService: UserCommandService
) {
    @GetMapping("/feed")
    fun getMyFeed(@Valid request: CommonPageRequest): ApiResponse<CommonPageResponse<UserFeedResponse>> {
        val userId = SecurityContextHolder.getContext().authentication.name
        return ApiResponse.ok(userQueryService.getMyFeed(request, userId))
    }

    @GetMapping("/scraps/feed")
    fun getFeedScraps(@Valid request: CommonPageRequest): ApiResponse<CommonPageResponse<FeedScrapResponse>> {
        val userId = SecurityContextHolder.getContext().authentication.name
        return ApiResponse.ok(userQueryService.getFeedScraps(request, userId))
    }

    @GetMapping("/scraps/liquor")
    fun getLiquorScraps(@Valid request: CommonPageRequest): ApiResponse<CommonPageResponse<LiquorScrapResponse>> {
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

    @GetMapping("/feed/isScrap")
    fun isFeedScrap(@RequestParam feedId: String): ApiResponse<Boolean> {
        val userId = SecurityContextHolder.getContext().authentication.name
        return ApiResponse.ok(userQueryService.isFeedScrap(userId, feedId))
    }
}
