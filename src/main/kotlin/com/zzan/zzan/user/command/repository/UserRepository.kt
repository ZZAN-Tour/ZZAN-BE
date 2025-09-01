package com.zzan.zzan.user.command.repository

import com.zzan.zzan.user.command.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun existsByIdAndDeletedAtIsNull(id: String): Boolean
    fun findByIdAndDeletedAtIsNull(id: String): User?
}
