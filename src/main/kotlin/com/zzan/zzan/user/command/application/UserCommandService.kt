package com.zzan.zzan.user.command.application

import com.zzan.zzan.user.command.domain.User

interface UserCommandService {
    fun createUser(user: User): String
}
