package com.zzan.zzan.feed.command.domain

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "feeds")
@EntityListeners(AuditingEntityListener::class)
data class Feed(
    @Id
    @Column(length = 26)
    val id: String = UlidCreator.getUlid().toString(),

    @Column(name = "user_id", length = 26)
    val userId: String,

    @Column(name = "image_url")
    val imageUrl: String,

    val score: Double? = null,

    @Column(columnDefinition = "TEXT")
    val text: String? = null,

    @Column(name = "place_id", length = 26)
    val placeId: String,

    @Column(name = "buy_place_id", length = 26)
    val buyPlaceId: String? = null,

    @CreatedDate
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @Column(name = "deleted_at")
    val deletedAt: LocalDateTime? = null
)
