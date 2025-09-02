package com.zzan.zzan.liquorrating.command.domain

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity
@Table(name = "liquor_ratings")
@EntityListeners(AuditingEntityListener::class)
class LiquorRating(
    @Id
    @Column(length = 26)
    val id: String = UlidCreator.getUlid().toString(),

    @Column(name = "user_id", length = 26)
    val userId: String,

    @Column(name = "liquor_id", length = 26)
    val liquorId: String,

    @Column(name = "source_feed_id", length = 26)
    val sourceFeedId: String,

    @Column(name = "place_id", length = 26)
    val placeId: String,

    // ✅ 수정 가능한 필드들을 private var + public getter로 캡슐화
    @Column(precision = 2, scale = 1)
    private var _score: Double,

    @Column(length = 500)
    private var _comment: String? = null,

    @CreatedDate
    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null,

    @Column(name = "updated_at")
    private var _updatedAt: LocalDateTime? = null,


) {
    // JPA 기본 생성자
    constructor() : this("", "", "", "", "", 0.0, null, null, null)

    // ✅ 읽기 전용 프로퍼티들 (외부에서 수정 불가)
    val score: Double get() = _score
    val comment: String? get() = _comment
    val updatedAt: LocalDateTime? get() = _updatedAt

    init {
        validateScore(_score)
    }

    // ✅ 도메인 로직들을 엔티티 내부로 이동

    /**
     * 평점 업데이트 (핵심 비즈니스 로직)
     */
    fun updateRating(newScore: Double, newComment: String?) {
        validateScore(newScore)
        this._score = newScore
        this._comment = newComment?.takeIf { it.isNotBlank() }
        this._updatedAt = LocalDateTime.now()

        // 도메인 이벤트 발행 (선택적)
        // DomainEvents.raise(LiquorRatingUpdatedEvent(this.id, this.liquorId))
    }

    /**
     * 코멘트만 업데이트
     */
    fun updateComment(newComment: String?) {
        this._comment = newComment?.takeIf { it.isNotBlank() }
        this._updatedAt = LocalDateTime.now()
    }

    /**
     * 점수만 업데이트
     */
    fun updateScore(newScore: Double) {
        validateScore(newScore)
        this._score = newScore
        this._updatedAt = LocalDateTime.now()
    }

    /**
     * 권한 검증 로직
     */
    fun canBeUpdatedBy(userId: String): Boolean {
        return this.userId == userId
    }

    /**
     * 점수 검증 로직
     */
    private fun validateScore(score: Double) {
        require(score in 1.0..5.0) { "평점은 1.0에서 5.0 사이여야 합니다: $score" }
    }

    /**
     * 비즈니스 규칙: 생성된 지 얼마나 됐는지
     */
    fun getDaysSinceCreated(): Long {
        return if (createdAt != null) {
            ChronoUnit.DAYS.between(createdAt, LocalDateTime.now())
        } else 0
    }

    /**
     * 비즈니스 규칙: 수정 가능한 기간인지 (예: 7일 이내)
     */
    fun isWithinEditablePeriod(): Boolean {
        return getDaysSinceCreated() <= 7
    }
}
