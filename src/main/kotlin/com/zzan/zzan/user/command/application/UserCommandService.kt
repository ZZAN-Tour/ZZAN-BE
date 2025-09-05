package com.zzan.zzan.user.command.application

import com.zzan.zzan.api.user.dto.ScrapResponse
import com.zzan.zzan.user.command.domain.User

interface UserCommandService {
    fun createUser(user: User): User

    fun createFeedScrap(userId: String, feedId: String): ScrapResponse

    fun createLiquorScrap(userId: String, liquorId: String): ScrapResponse

    fun deleteFeedScrap(userId: String, feedId: String): ScrapResponse

    fun deleteLiquorScrap(userId: String, liquorId: String): ScrapResponse
}
