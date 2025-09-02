package com.zzan.zzan.liquorrating.command.service

import com.zzan.zzan.api.liquorrating.dto.CreateLiquorRatingRequest
import com.zzan.zzan.api.liquorrating.dto.UpdateLiquorRatingRequest

/**
 * 전통주 평점 Command Service 인터페이스
 *
 * CQS(Command Query Separation) 패턴에 따라 생성/수정/삭제 작업만 담당
 * 모든 메서드는 작업 완료를 나타내는 값만 반환하고, 조회는 별도 Query Service에서 처리
 */
interface LiquorRatingCommandService {

    /**
     * 전통주 평점 생성
     *
     * 비즈니스 규칙:
     * - 사용자는 자신이 작성한 피드에서 태그한 전통주에만 평점을 줄 수 있음
     * - 같은 전통주에 여러 번 평점을 줄 수 있음 (장소별 경험이 다르므로)
     * - 평점은 1.0~5.0 사이여야 함
     *
     * @param userId 평점을 생성하는 사용자 ID
     * @param request 평점 생성 요청 데이터
     * @return 생성된 평점 ID
     *
     * @throws CustomException
     *   - NOT_FOUND: 피드 또는 전통주를 찾을 수 없음
     *   - FORBIDDEN: 본인이 작성한 피드가 아님
     *   - BAD_REQUEST: 해당 피드에서 태그하지 않은 전통주이거나 유효하지 않은 평점
     */
    fun createRating(userId: String, request: CreateLiquorRatingRequest): String

    /**
     * 전통주 평점 수정
     *
     * 비즈니스 규칙:
     * - 본인이 작성한 평점만 수정 가능
     * - 평점 작성 후 7일 이내에만 수정 가능
     * - 평점은 1.0~5.0 사이여야 함
     *
     * @param userId 평점을 수정하는 사용자 ID
     * @param ratingId 수정할 평점 ID
     * @param request 평점 수정 요청 데이터
     *
     * @throws CustomException
     *   - NOT_FOUND: 평점을 찾을 수 없음
     *   - FORBIDDEN: 본인이 작성한 평점이 아님
     *   - BAD_REQUEST: 수정 기간을 초과했거나 유효하지 않은 평점
     */
    fun updateRating(userId: String, ratingId: String, request: UpdateLiquorRatingRequest)

    /**
     * 전통주 평점 삭제
     *
     * 비즈니스 규칙:
     * - 본인이 작성한 평점만 삭제 가능
     * - 삭제된 평점은 복구 불가능 (soft delete 없음)
     *
     * @param userId 평점을 삭제하는 사용자 ID
     * @param ratingId 삭제할 평점 ID
     *
     * @throws CustomException
     *   - NOT_FOUND: 평점을 찾을 수 없음
     *   - FORBIDDEN: 본인이 작성한 평점이 아님
     */
    fun deleteRating(userId: String, ratingId: String)

    /**
     * 점수만 빠르게 업데이트 (코멘트 유지)
     *
     * 사용 사례: 별점만 변경하고 싶을 때
     *
     * @param userId 사용자 ID
     * @param ratingId 평점 ID
     * @param newScore 새로운 점수
     *
     * @throws CustomException 동일한 권한 및 유효성 검사
     */
    fun updateScoreOnly(userId: String, ratingId: String, newScore: Double)

    /**
     * 코멘트만 업데이트 (점수 유지)
     *
     * 사용 사례: 리뷰 내용만 변경하고 싶을 때
     *
     * @param userId 사용자 ID
     * @param ratingId 평점 ID
     * @param newComment 새로운 코멘트 (null 가능)
     *
     * @throws CustomException 동일한 권한 검사
     */
    fun updateCommentOnly(userId: String, ratingId: String, newComment: String?)
}
