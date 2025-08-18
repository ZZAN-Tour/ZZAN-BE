package com.zzan.zzan.api.liquor.dto

data class LiquorDetailResponse(
    val id: String,

    val name: String, // 술 이름

    val type: String, // 술 종류 (탁주, 약주, 증류주 등)

    val score: Double?, // 술 평점 (0.0 ~ 5.0)

    val description: String?, // 술 설명

    val foodPairing: String?, // 음식 페어링 (어울리는 음식)

    val volume: String?, // 술 용량 (500ml, 750ml 등)

    val content: String?, // 도수 (7%, 8%, ...)

    val awards: String?, // 수상내역

    val etc: String?, // 기타사항 (무감미료, 진함 등)

    val imageUrl: String?, // 이미지 URL

    val brewery: String? // 양조장 이름
)
