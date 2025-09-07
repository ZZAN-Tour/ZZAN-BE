package com.zzan.zzan.user.command.domain

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "liquor_scraps")
class LiquorScrap(
    @Id
    @Column(length = 26)
    val id: String = UlidCreator.getUlid().toString(),

    @Column(name = "user_id", length = 26)
    val userId: String, // 스크랩한 사용자 ID

    @Column(name = "liquor_id", length = 26)
    val liquorId: String, // 스크랩한 전통주 ID
) {
    companion object {
        fun of(userId: String, liquorId: String): LiquorScrap {
            require(userId.isNotBlank()) { "사용자 ID는 필수입니다" }
            require(liquorId.isNotBlank()) { "전통주 ID는 필수입니다" }
            
            return LiquorScrap(
                userId = userId,
                liquorId = liquorId,
            )
        }
    }
}
