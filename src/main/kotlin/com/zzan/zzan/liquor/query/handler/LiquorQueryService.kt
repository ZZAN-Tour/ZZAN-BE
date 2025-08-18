package com.zzan.zzan.liquor.query.handler

import com.zzan.zzan.api.liquor.dto.LiquorDetailResponse

interface LiquorQueryService {

    /**
     * 특정 ID에 해당하는 술 정보를 조회합니다.
     *
     * @param id 조회할 술의 ID
     * @return LiquorDetailResponse 술 상세 정보 응답 객체
     */
    fun getLiquorById(id: String): LiquorDetailResponse
}