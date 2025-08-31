package com.zzan.zzan.user.command.infrastructure

import com.zzan.zzan.user.command.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserCommandRepository : JpaRepository<User, String>
