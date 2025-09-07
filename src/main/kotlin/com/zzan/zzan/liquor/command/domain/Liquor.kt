package com.zzan.zzan.liquor.command.domain

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 전통주 엔티티 - 단순화된 버전
 */
@Entity
@Table(name = "liquors")
class Liquor(
    @Id
    @Column(length = 26)
    val id: String = UlidCreator.getUlid().toString(),

    val name: String,

    val type: String, // 탁주, 약주, 증류주 등

    // ===== 평점 통계 관련 필드들 (단순화) =====

    @Column
    var score: Double? = null, // 평균 평점 (1.0~5.0)

    @Column(name = "rating_count")
    var ratingCount: Int = 0, // 평점 개수

    @Column(name = "rating_updated_at")
    var ratingUpdatedAt: LocalDateTime? = null, // 평점 통계 마지막 업데이트 시간

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

    // ===== 유틸리티 메서드들 (비즈니스 로직은 최소한만) =====

    /**
     * 추천 가능한 전통주인지 확인
     */
    fun isRecommendable(minRatingCount: Int = 3, minAverageScore: Double = 4.0): Boolean {
        return ratingCount >= minRatingCount && (score ?: 0.0) >= minAverageScore
    }

    /**
     * 평점 등급 반환
     */
    fun getRatingGrade(): String {
        return when {
            score == null -> "평점 없음"
            score!! >= 4.5 -> "최고"
            score!! >= 4.0 -> "우수"
            score!! >= 3.5 -> "좋음"
            score!! >= 3.0 -> "보통"
            else -> "아쉬움"
        }
    }

    /**
     * 평점 통계 요약 정보
     */
    fun getRatingStatsSummary(): String {
        return if (ratingCount == 0) {
            "평점 없음"
        } else {
            "평점 ${String.format("%.1f", score)} (${ratingCount}개)"
        }
    }

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
        return "Liquor(id='$id', name='$name', type='$type', score=$score, ratingCount=$ratingCount, brewery=$brewery)"
    }
}
