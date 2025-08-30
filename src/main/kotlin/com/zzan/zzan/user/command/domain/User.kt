package com.zzan.zzan.user.command.domain

import com.github.f4b6a3.ulid.UlidCreator
import com.zzan.zzan.common.client.dto.KakaoUserResponse
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(
    @Id
    @Column(length = 26)
    val id: String = UlidCreator.getUlid().toString(),

    @Column(name = "kakao_id", unique = true)
    val kakaoId: String, // 카카오톡 사용자 ID

    @Column(name = "profile_image_url")
    val profileImageUrl: String? = null, // 프로필 이미지 URL

    val nickname: String? = null, // 사용자 닉네임

    @Enumerated(EnumType.STRING)
    val role: UserRole = UserRole.USER,

    @CreatedDate
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @Column(name = "deleted_at")
    val deletedAt: LocalDateTime? = null
) {
    companion object {
        fun of(kakaoUser: KakaoUserResponse): User {
            return User(
                kakaoId = kakaoUser.id.toString(),
                profileImageUrl = kakaoUser.kakaoAccount.profile.profileImageUrl,
                nickname = kakaoUser.kakaoAccount.profile.nickname
            )
        }
    }
}
