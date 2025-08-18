package com.zzan.zzan.liquor.command.domain

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "liquors")
class Liquor(
    @Id
    @Column(length = 26)
    val id: String = UlidCreator.getUlid().toString(),

    val name: String,

    val type: String, // 탁주, 약주, 증류주 등

    val score: Double? = null, // 평점 (예: 4.5)

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
)
