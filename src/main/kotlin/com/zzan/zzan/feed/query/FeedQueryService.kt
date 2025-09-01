package com.zzan.zzan.feed.query

import com.zzan.zzan.api.feed.dto.*

interface FeedQueryService {
    /**
     * 특정 ID에 해당하는 피드 상세 정보를 조회합니다.
     *
     * @param feedId 조회할 피드의 ID
     * @return FeedDetailResponse 피드 상세 정보 응답 객체
     */
    fun getFeedById(feedId: String): FeedDetailResponse

    /**
     * 검색 조건과 페이징 정보에 따라 피드 목록을 조회합니다.
     *
     * @param criteria 검색 조건
     * @param pageRequest 페이징 정보
     * @return PageResponse<FeedSummaryResponse> 페이징된 피드 목록
     */
    fun getFeeds(criteria: FeedSearchCriteria, pageRequest: PageRequest): PageResponse<FeedSummaryResponse>

    /**
     * 피드 존재 여부를 확인합니다.
     *
     * @param feedId 확인할 피드의 ID
     * @return Boolean 피드 존재 여부
     */
    fun existsFeed(feedId: String): Boolean

    /**
     * 특정 사용자의 피드 개수를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return Long 사용자의 피드 개수
     */
    fun getFeedCountByUser(userId: String): Long

    /**
     * 특정 장소의 피드 개수를 조회합니다.
     *
     * @param placeId 장소 ID
     * @return Long 장소의 피드 개수
     */
    fun getFeedCountByPlace(placeId: String): Long

    /**
     * 텍스트 검색을 통해 피드를 조회합니다.
     *
     * @param query 검색어
     * @param pageRequest 페이징 정보
     * @return PageResponse<FeedSummaryResponse> 검색된 피드 목록
     */
    fun searchFeeds(query: String, pageRequest: PageRequest): PageResponse<FeedSummaryResponse>
}
