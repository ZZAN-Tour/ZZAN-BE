// src/main/kotlin/com/zzan/zzan/api/liquortag/LiquorTagController.kt
package com.zzan.zzan.api.liquortag

import com.zzan.zzan.api.liquortag.dto.CreateLiquorTagRequest
import com.zzan.zzan.api.liquortag.dto.LiquorTagResponse
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.liquortag.command.service.LiquorTagCommandService
import com.zzan.zzan.liquortag.query.service.LiquorTagQueryService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/liquor-tags")
class LiquorTagController(
    private val liquorTagCommandService: LiquorTagCommandService,
    private val liquorTagQueryService: LiquorTagQueryService
) {

    /**
     * 전통주 태그 생성 (이미지 더블클릭 → 검색 → 태그 생성)
     */
    @PostMapping
    fun createTag(
        @Valid @RequestBody request: CreateLiquorTagRequest
    ): ApiResponse<Map<String, String>> {
        val tagId = liquorTagCommandService.createTag(request)
        return ApiResponse.ok(
            mapOf(
                "tagId" to tagId,
                "message" to "전통주 태그가 성공적으로 생성되었습니다."
            )
        )
    }

    /**
     * 피드별 전통주 태그 목록 조회
     */
    @GetMapping("/feed/{feedId}")
    fun getTagsByFeed(@PathVariable feedId: String): ApiResponse<List<LiquorTagResponse>> {
        val tags = liquorTagQueryService.getTagsByFeedId(feedId)
        return ApiResponse.ok(tags)
    }

    /**
     * 이미지별 전통주 태그 목록 조회 (프론트엔드에서 태그 표시용)
     */
    @GetMapping("/image/{imageId}")
    fun getTagsByImage(@PathVariable imageId: String): ApiResponse<List<LiquorTagResponse>> {
        val tags = liquorTagQueryService.getTagsByImageId(imageId)
        return ApiResponse.ok(tags)
    }

    /**
     * 전통주 태그 상세 조회
     */
    @GetMapping("/{tagId}")
    fun getTagById(@PathVariable tagId: String): ApiResponse<LiquorTagResponse?> {
        val tag = liquorTagQueryService.getTagById(tagId)
        return ApiResponse.ok(tag)
    }

    /**
     * 전통주 태그 삭제
     */
    @DeleteMapping("/{tagId}")
    fun deleteTag(@PathVariable tagId: String): ApiResponse<Map<String, String>> {
        liquorTagCommandService.deleteTag(tagId)
        return ApiResponse.ok(
            mapOf(
                "tagId" to tagId,
                "message" to "전통주 태그가 성공적으로 삭제되었습니다."
            )
        )
    }

    /**
     * 이미지의 모든 태그 삭제
     */
    @DeleteMapping("/image/{imageId}")
    fun deleteTagsByImage(@PathVariable imageId: String): ApiResponse<Map<String, String>> {
        liquorTagCommandService.deleteTagsByImageId(imageId)
        return ApiResponse.ok(
            mapOf(
                "imageId" to imageId,
                "message" to "이미지의 모든 태그가 삭제되었습니다."
            )
        )
    }
}
