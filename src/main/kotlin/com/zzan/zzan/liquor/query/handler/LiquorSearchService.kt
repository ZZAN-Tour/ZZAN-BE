
// src/main/kotlin/com/zzan/zzan/liquor/query/handler/LiquorSearchService.kt (수정)
package com.zzan.zzan.liquor.query.handler

import com.zzan.zzan.api.liquor.dto.LiquorSearchResponse

interface LiquorSearchService {
    /**
     * 자동완성용 전통주 검색
     * 입력한 키워드로 시작하는 전통주 목록을 반환
     *
     * @param keyword 검색할 키워드
     * @param limit 최대 결과 개수 (기본 10개)
     * @return 검색된 전통주 목록
     */
    fun autocomplete(keyword: String, limit: Int = 10): List<LiquorSearchResponse>

    /**
     * 전체 검색
     * 키워드가 포함된 모든 전통주를 검색
     *
     * @param keyword 검색할 키워드
     * @return 검색된 전통주 목록
     */
    fun search(keyword: String): List<LiquorSearchResponse>
}
