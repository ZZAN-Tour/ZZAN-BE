package com.zzan.zzan.api.feed.dto


data class FeedCountInfo(
    /**
     * 피드 좋아요 개수
     * FeedLikes 테이블에서 집계
     */
    val likes: Long = 0,

    /**
     * 피드 스크랩 개수
     * FeedScraps 테이블에서 집계
     */
    val scraps: Long = 0,

    /**
     * 이 피드에 태그된 전통주 개수
     * LiquorTags 테이블에서 DISTINCT liquorId로 집계
     */
    val taggedLiquors: Int = 0,

    /**
     * 이 피드를 기반으로 작성된 평점 개수
     * LiquorRatings 테이블에서 sourceFeedId로 집계
     */
    val ratings: Long = 0,

    /**
     * 피드 조회수 (선택적 기능)
     * 현재는 구현하지 않으므로 null로 설정
     * 추후 조회수 기능 추가 시 활용
     */
    val views: Long? = null
)
