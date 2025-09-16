package com.zzan.zzan.common.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoUserResponse(
    val id: Long,

    @JsonProperty("connected_at")
    val connectedAt: String?,

    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount
) {
    data class KakaoAccount(
        val profile: Profile
    )

    data class Profile(
        val nickname: String,

        @JsonProperty("profile_image_url")
        val profileImageUrl: String? = null
    )
}
