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

    // ===== 수정 가능한 필드들 (단순화) =====
    @Column
    var score: Double,

    @Column(length = 500)
    var comment: String? = null,

    @CreatedDate
    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

) {
    // JPA 기본 생성자
    constructor() : this("", "", "", "", "", 0.0, null, null, null)

    init {
        require(score in 0.0..5.0) { "평점은 1.0에서 5.0 사이여야 합니다: $score" }
    }
}
