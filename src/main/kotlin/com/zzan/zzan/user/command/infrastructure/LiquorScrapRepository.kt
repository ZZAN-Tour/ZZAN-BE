package com.zzan.zzan.user.command.infrastructure

import com.zzan.zzan.user.command.domain.LiquorScrap
import org.springframework.data.jpa.repository.JpaRepository

interface LiquorScrapRepository : JpaRepository<LiquorScrap, String> {
    fun existsByUserIdAndLiquorId(userId: String, liquorId: String): Boolean
    fun findByUserIdAndLiquorId(userId: String, liquorId: String): LiquorScrap?
}

