// src/main/kotlin/com/zzan/zzan/liquor/query/handler/LiquorSearchServiceImpl.kt (수정)
package com.zzan.zzan.liquor.query.handler

import com.zzan.zzan.api.liquor.dto.LiquorSearchResponse
import com.zzan.zzan.liquor.query.repository.LiquorSearchRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class LiquorSearchServiceImpl(
    private val liquorSearchRepository: LiquorSearchRepository
) : LiquorSearchService {

    @Cacheable("liquorAutocomplete", key = "#keyword + '_' + #limit")
    override fun autocomplete(keyword: String, limit: Int): List<LiquorSearchResponse> {
        if (keyword.isBlank()) return emptyList()

        val trimmedKeyword = keyword.trim()
        val results = liquorSearchRepository.findLiquorsStartingWith(trimmedKeyword)
            .take(limit)

        return results
    }

    @Cacheable("liquorSearch", key = "#keyword")
    override fun search(keyword: String): List<LiquorSearchResponse> {
        if (keyword.isBlank()) return emptyList()

        val trimmedKeyword = keyword.trim()
        return liquorSearchRepository.searchLiquors(trimmedKeyword)
    }
}
