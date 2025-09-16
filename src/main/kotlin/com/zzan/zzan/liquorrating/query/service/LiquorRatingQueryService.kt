package com.zzan.zzan.liquorrating.query.service

import com.zzan.zzan.api.liquorrating.dto.LiquorRatingResponse
import com.zzan.zzan.api.liquorrating.dto.LiquorRatingStatsResponse
import com.zzan.zzan.api.liquorrating.dto.UserRatingStatsResponse

/**
 * 전통주 평점 Query Service 인터페이스
 *
 * CQS(Command Query Separation) 패턴에 따라 조회 작업만 담당
 * 모든 메서드는 읽기 전용이며, 캐싱 최적화가 적용됨
 */
interface LiquorRatingQueryService {

    /**
     * 특정 전통주의 모든 평점 조회
     *
     * 사용 사례: 전통주 상세 페이지에서 해당 전통주의 모든 리뷰 표시
     * 정렬: 최신 평점순
     *
     * @param liquorId 조회할 전통주 ID
     * @return 해당 전통주의 평점 목록 (최신순)
     */
    fun getRatingsByLiquor(liquorId: String): List<LiquorRatingResponse>

    /**
     * 특정 사용자의 모든 평점 조회
     *
     * 사용 사례: 마이 페이지에서 내가 작성한 모든 평점 표시
     * 정렬: 최신 작성순
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 평점 목록 (최신순)
     */
    fun getRatingsByUser(userId: String): List<LiquorRatingResponse>

    /**
     * 특정 전통주의 평점 통계 조회
     *
     * 사용 사례: 전통주 상세 페이지에서 평균 점수, 평점 분포, 총 평점 수 등 표시
     * 캐싱: 자주 조회되므로 Redis 캐시 적용
     *
     * @param liquorId 조회할 전통주 ID
     * @return 평점 통계 정보 (평균 점수, 총 개수, 점수별 분포, 최신 평점들)
     */
    fun getRatingStats(liquorId: String): LiquorRatingStatsResponse

    /**
     * 특정 평점 상세 조회
     *
     * 사용 사례: 평점 수정/삭제 시 기존 정보 로딩
     *
     * @param ratingId 조회할 평점 ID
     * @return 평점 상세 정보 (없으면 null)
     */
    fun getRatingById(ratingId: String): LiquorRatingResponse?

    /**
     * 수정 가능한 사용자 평점 조회
     *
     * 사용 사례: 평점 관리 페이지에서 수정 가능한 평점들만 표시
     * 비즈니스 규칙: 작성한 지 7일 이내의 평점들만 반환
     *
     * @param userId 조회할 사용자 ID
     * @return 수정 가능한 평점 목록 (7일 이내 작성된 것만)
     */
    fun getEditableRatingsByUser(userId: String): List<LiquorRatingResponse>

    /**
     * 특정 피드와 관련된 평점들 조회
     *
     * 사용 사례: 피드 삭제 시 관련된 평점들 확인, 피드 상세에서 해당 피드로 작성된 평점 표시
     *
     * @param feedId 조회할 피드 ID
     * @return 해당 피드를 근거로 작성된 평점들
     */
    fun getRatingsBySourceFeed(feedId: String): List<LiquorRatingResponse>

    /**
     * 특정 장소에서 작성된 평점들 조회
     *
     * 사용 사례: 장소별 전통주 경험 분석, 장소 상세 페이지에서 해당 장소의 전통주 평점 표시
     *
     * @param placeId 조회할 장소 ID
     * @return 해당 장소에서 작성된 평점들
     */
    fun getRatingsByPlace(placeId: String): List<LiquorRatingResponse>

    /**
     * 높은 평점 순으로 전통주 평점 조회 (추천용)
     *
     * 사용 사례: "높은 평점을 받은 전통주" 추천 기능
     * 필터: 최소 평점 개수 이상인 전통주만 포함
     *
     * @param minRatingCount 최소 평점 개수 (기본: 3개)
     * @param limit 조회할 최대 개수 (기본: 10개)
     * @return 높은 평점 순으로 정렬된 전통주 평점 통계들
     */
    fun getTopRatedLiquors(minRatingCount: Int = 3, limit: Int = 10): List<LiquorRatingStatsResponse>

    /**
     * 사용자의 평점 통계 조회
     *
     * 사용 사례: 마이 페이지에서 "내가 작성한 평점 통계" 표시
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 평점 통계 정보
     */
    fun getUserRatingStats(userId: String): UserRatingStatsResponse

    /**
     * 평점 검색 (텍스트 기반)
     *
     * 사용 사례: 평점 코멘트 내용으로 검색 기능
     * 검색 대상: 평점의 comment 필드
     *
     * @param query 검색할 키워드
     * @param minScore 최소 점수 필터 (선택적)
     * @param maxResults 최대 결과 개수 (기본: 50)
     * @return 검색된 평점 목록
     */
    fun searchRatings(query: String, minScore: Double? = null, maxResults: Int = 50): List<LiquorRatingResponse>

    /**
     * 특정 전통주 타입별 평점 조회
     *
     * 사용 사례: 전통주 타입별 평점 분석 (탁주, 약주, 증류주 등)
     *
     * @param liquorType 전통주 타입 (탁주, 약주, 증류주 등)
     * @param limit 조회할 최대 개수 (기본: 20)
     * @return 해당 타입의 전통주들에 대한 평점 목록
     */
    fun getRatingsByLiquorType(liquorType: String, limit: Int = 20): List<LiquorRatingResponse>

    /**
     * 최근 N일 내 작성된 평점 조회
     *
     * 사용 사례: 최근 활동 분석, 관리자 모니터링
     *
     * @param days 조회할 일수 (기본: 7일)
     * @param limit 조회할 최대 개수 (기본: 100)
     * @return 최근 N일 내 작성된 평점 목록
     */
    fun getRecentRatings(days: Int = 7, limit: Int = 100): List<LiquorRatingResponse>

    /**
     * 코멘트가 있는 평점들만 조회
     *
     * 사용 사례: 상세한 리뷰가 있는 평점들만 표시
     *
     * @param minCommentLength 최소 코멘트 길이 (기본: 10자)
     * @param limit 조회할 최대 개수 (기본: 30)
     * @return 코멘트가 있는 평점 목록
     */
    fun getRatingsWithComments(minCommentLength: Int = 10, limit: Int = 30): List<LiquorRatingResponse>

    /**
     * 특정 점수 범위의 평점 조회
     *
     * 사용 사례: 점수별 평점 분석, 낮은 점수/높은 점수 평점만 조회
     *
     * @param minScore 최소 점수 (포함)
     * @param maxScore 최대 점수 (포함)
     * @param limit 조회할 최대 개수 (기본: 50)
     * @return 점수 범위에 해당하는 평점 목록
     */
    fun getRatingsByScoreRange(minScore: Double, maxScore: Double, limit: Int = 50): List<LiquorRatingResponse>

    /**
     * 사용자별 평점 개수 조회
     *
     * 사용 사례: 사용자 활동 통계, 랭킹 시스템
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 총 평점 개수
     */
    fun getRatingCountByUser(userId: String): Long

    /**
     * 전통주별 평점 개수 조회
     *
     * 사용 사례: 전통주 인기도 분석
     *
     * @param liquorId 조회할 전통주 ID
     * @return 해당 전통주의 총 평점 개수
     */
    fun getRatingCountByLiquor(liquorId: String): Long

    /**
     * 장소별 평점 개수 조회
     *
     * 사용 사례: 장소별 전통주 활동 분석
     *
     * @param placeId 조회할 장소 ID
     * @return 해당 장소에서 작성된 총 평점 개수
     */
    fun getRatingCountByPlace(placeId: String): Long

    /**
     * 특정 사용자가 특정 전통주에 평점을 준 적이 있는지 확인
     *
     * 사용 사례: 중복 평점 방지, UI 상태 표시
     *
     * @param userId 사용자 ID
     * @param liquorId 전통주 ID
     * @return 평점을 준 적이 있으면 true
     */
    fun hasUserRatedLiquor(userId: String, liquorId: String): Boolean

    /**
     * 월별 평점 통계 조회 (전체)
     *
     * 사용 사례: 시스템 사용량 분석, 트렌드 분석
     *
     * @param months 조회할 개월 수 (기본: 12개월)
     * @return 월별 평점 개수 맵 ("2025-01" -> 150개)
     */
    fun getMonthlyRatingStats(months: Int = 12): Map<String, Long>

    /**
     * 평점 분포 통계 조회 (전체)
     *
     * 사용 사례: 전체 평점 품질 분석
     *
     * @return 점수별 평점 분포 ("1" -> 10개, "2" -> 25개, ...)
     */
    fun getOverallScoreDistribution(): Map<String, Long>

    /**
     * 활발한 사용자 TOP N 조회
     *
     * 사용 사례: 사용자 랭킹, 리워드 시스템
     *
     * @param limit 조회할 상위 사용자 수 (기본: 10명)
     * @return 평점 개수별 상위 사용자 통계 목록
     */
    fun getTopActiveUsers(limit: Int = 10): List<UserRatingStatsResponse>
}
