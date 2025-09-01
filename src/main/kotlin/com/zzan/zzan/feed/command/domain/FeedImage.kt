package com.zzan.zzan.feed.command.domain

import jakarta.persistence.*
import com.github.f4b6a3.ulid.UlidCreator

@Entity
@Table(name = "feed_images")
data class FeedImage(
    @Id
    @Column(length = 26)
    val id: String = UlidCreator.getUlid().toString(),

    @Column(name = "feed_id", length = 26)
    val feedId: String,

    @Column(name = "image_url")
    val imageUrl: String,

    @Column(name = "order_num")
    val orderNum: Int
) {
    // JPA 요구사항을 위한 기본 생성자
    constructor() : this("", "", "", 0)
}
