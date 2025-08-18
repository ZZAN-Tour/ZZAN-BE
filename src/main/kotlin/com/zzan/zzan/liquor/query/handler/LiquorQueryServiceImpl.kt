package com.zzan.zzan.liquor.query.handler

import com.zzan.zzan.api.liquor.dto.LiquorDetailResponse
import com.zzan.zzan.common.exception.CustomException
import com.zzan.zzan.liquor.query.repository.LiquorQueryRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service


@Service
class LiquorQueryServiceImpl(
    private val liquorQueryRepository: LiquorQueryRepository
) : LiquorQueryService {

    @Cacheable("liquor", key = "#id")
    override fun getLiquorById(id: String): LiquorDetailResponse {
        return liquorQueryRepository.getLiquorById(id) ?: throw CustomException(
            status = HttpStatus.NOT_FOUND,
            message = "해당 전통주는 찾을 수 없습니다.",
        )
    }
}
