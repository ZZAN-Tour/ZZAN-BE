package com.zzan.zzan.liquor.command.domain

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 전통주 엔티티
 *
 * 평점 시스템과 연동하여 평점 통계 필드들을 포함
 */
@Entity
@Table(name = "liquors")
class Liquor(
    @Id
    @Column(length = 26)
    val id: String = UlidCreator.getUlid().toString(),

    val name: String,

    val type: String, // 탁주, 약주, 증류주 등

    // ===== 평점 통계 관련 필드들 (Mutable) =====

    @Column(precision = 2, scale = 1)
    private var _score: Double? = null, // 평균 평점 (1.0~5.0)

    @Column(name = "rating_count")
    private var _ratingCount: Int = 0, // 평점 개수

    @Column(name = "rating_updated_at")
    private var _ratingUpdatedAt: LocalDateTime? = null, // 평점 통계 마지막 업데이트 시간

    // ===== 기본 전통주 정보 필드들 =====

    @Column(columnDefinition = "TEXT")
    val description: String?,

    @Column(name = "food_pairing", columnDefinition = "TEXT")
    val foodPairing: String?,

    val volume: String?, // 술 용량 (500ml, 750ml 등)

    val content: String?, // 도수 (7%, 8%, ...)

    val awards: String?, // 수상내역

    val etc: String?, // 기타사항 (무감미료, 진함 등)

    @Column(name = "image_url")
    val imageUrl: String?, // 이미지 URL

    val brewery: String? // 양조장 이름
) {
    // JPA 기본 생성자
    constructor() : this("", "", "", null, 0, null, null, null, null, null, null, null, null, null)

    // ===== 읽기 전용 프로퍼티들 (평점 통계) =====

    val score: Double? get() = _score
    val ratingCount: Int get() = _ratingCount
    val ratingUpdatedAt: LocalDateTime? get() = _ratingUpdatedAt

    // ===== 도메인 메서드들 (평점 통계 관리) =====

    /**
     * 평점 통계 업데이트
     *
     * 배치 처리에서 호출되어 전통주의 평점 통계를 업데이트함
     *
     * @param newAverageScore 새로운 평균 평점 (null 가능 - 평점이 없을 때)
     * @param newRatingCount 새로운 평점 개수
     */
    fun updateRatingStats(newAverageScore: Double?, newRatingCount: Int) {
        require(newRatingCount >= 0) { "평점 개수는 0 이상이어야 합니다: $newRatingCount" }

        if (newRatingCount > 0) {
            require(newAverageScore != null) { "평점이 있을 때는 평균 점수가 null일 수 없습니다" }
            require(newAverageScore in 1.0..5.0) { "평균 점수는 1.0~5.0 사이여야 합니다: $newAverageScore" }
        }

        this._score = if (newRatingCount > 0) newAverageScore else null
        this._ratingCount = newRatingCount
        this._ratingUpdatedAt = LocalDateTime.now()
    }

    /**
     * 평점 통계 초기화
     *
     * 사용 사례: 전통주의 모든 평점이 삭제되었을 때
     */
    fun clearRatingStats() {
        this._score = null
        this._ratingCount = 0
        this._ratingUpdatedAt = LocalDateTime.now()
    }

    /**
     * 평점 통계가 유효한지 확인
     *
     * @return 통계가 유효하면 true
     */
    fun hasValidRatingStats(): Boolean {
        return when {
            _ratingCount == 0 -> _score == null
            _ratingCount > 0 -> _score != null && _score!! in 1.0..5.0
            else -> false
        }
    }

    /**
     * 추천 가능한 전통주인지 확인
     *
     * 비즈니스 규칙: 최소 3개 이상의 평점과 4.0 이상의 평점
     *
     * @param minRatingCount 최소 평점 개수 (기본: 3)
     * @param minAverageScore 최소 평균 점수 (기본: 4.0)
     * @return 추천 가능하면 true
     */
    fun isRecommendable(minRatingCount: Int = 3, minAverageScore: Double = 4.0): Boolean {
        return _ratingCount >= minRatingCount && (_score ?: 0.0) >= minAverageScore
    }

    /**
     * 평점 등급 반환
     *
     * @return 평점 등급 문자열
     */
    fun getRatingGrade(): String {
        return when {
            _score == null -> "평점 없음"
            _score!! >= 4.5 -> "최고"
            _score!! >= 4.0 -> "우수"
            _score!! >= 3.5 -> "좋음"
            _score!! >= 3.0 -> "보통"
            else -> "아쉬움"
        }
    }

    /**
     * 평점 통계 요약 정보
     *
     * @return 평점 통계 요약 문자열
     */
    fun getRatingStatsSummary(): String {
        return if (_ratingCount == 0) {
            "평점 없음"
        } else {
            "평점 ${String.format("%.1f", _score)} (${_ratingCount}개)"
        }
    }

    /**
     * 마지막 평점 통계 업데이트 이후 경과 일수
     *
     * @return 경과 일수 (업데이트 기록이 없으면 null)
     */
    fun getDaysSinceRatingUpdate(): Long? {
        return _ratingUpdatedAt?.let {
            java.time.temporal.ChronoUnit.DAYS.between(it, LocalDateTime.now())
        }
    }

    /**
     * 평점 통계가 최신인지 확인
     *
     * @param thresholdDays 기준 일수 (기본: 1일)
     * @return 최신이면 true
     */
    fun isRatingStatsUpToDate(thresholdDays: Long = 1): Boolean {
        val daysSinceUpdate = getDaysSinceRatingUpdate()
        return daysSinceUpdate != null && daysSinceUpdate <= thresholdDays
    }

    // ===== Object 메서드들 오버라이드 (data class 대신) =====

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Liquor
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Liquor(id='$id', name='$name', type='$type', score=$_score, ratingCount=$_ratingCount, brewery=$brewery)"
    }
}

// ===== 확장 함수들 (유틸리티) =====

/**
 * 전통주 목록을 평점 순으로 정렬
 */
fun List<Liquor>.sortByRating(ascending: Boolean = false): List<Liquor> {
    return if (ascending) {
        this.sortedWith(compareBy<Liquor> { it.score ?: 0.0 }.thenBy { it.ratingCount })
    } else {
        this.sortedWith(compareByDescending<Liquor> { it.score ?: 0.0 }.thenByDescending { it.ratingCount })
    }
}

/**
 * 추천 가능한 전통주들만 필터링
 */
fun List<Liquor>.filterRecommendable(minRatingCount: Int = 3, minAverageScore: Double = 4.0): List<Liquor> {
    return this.filter { it.isRecommendable(minRatingCount, minAverageScore) }
}

/**
 * 특정 평점 이상의 전통주들만 필터링
 */
fun List<Liquor>.filterByMinScore(minScore: Double): List<Liquor> {
    return this.filter { (it.score ?: 0.0) >= minScore }
}

/**
 * 전통주 타입별로 그룹핑
 */
fun List<Liquor>.groupByType(): Map<String, List<Liquor>> {
    return this.groupBy { it.type }
}
