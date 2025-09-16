package com.zzan.zzan.user.command.infrastructure

import com.zzan.zzan.user.command.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String> {
    fun existsByIdAndDeletedAtIsNull(id: String): Boolean
    fun findByIdAndDeletedAtIsNull(id: String): User?
}
