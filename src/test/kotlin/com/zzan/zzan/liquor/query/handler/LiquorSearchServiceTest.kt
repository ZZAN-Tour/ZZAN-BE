
// src/test/kotlin/com/zzan/zzan/liquor/query/handler/LiquorSearchServiceTest.kt
package com.zzan.zzan.liquor.query.handler

import com.zzan.zzan.api.liquor.dto.LiquorSearchResponse
import com.zzan.zzan.common.annotation.UnitTest
import com.zzan.zzan.liquor.query.repository.LiquorSearchRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@UnitTest
class LiquorSearchServiceTest {

    private val liquorSearchRepository = mockk<LiquorSearchRepository>()
    private val liquorSearchService = LiquorSearchServiceImpl(liquorSearchRepository)

    @Test
    fun `자동완성 검색 성공 테스트 - 참으로 시작하는 전통주들`() {
        // Given
        val keyword = "참"
        val limit = 5
        val expectedResults = listOf(
            LiquorSearchResponse("liquor-001", "참이슬", "증류주", "하이트진로", null),
            LiquorSearchResponse("liquor-002", "참참참", "탁주", "참좋은양조장", null)
        )

        every { liquorSearchRepository.findLiquorsStartingWith(keyword) } returns expectedResults

        // When
        val result = liquorSearchService.autocomplete(keyword, limit)

        // Then
        assertEquals(2, result.size)
        assertEquals("참이슬", result[0].name)
        assertEquals("참참참", result[1].name)
        assertTrue(result.all { it.name.startsWith("참") })
        verify(exactly = 1) { liquorSearchRepository.findLiquorsStartingWith(keyword) }
    }

    @Test
    fun `정확한 매칭 검색 테스트 - 참이슬 검색시 참이슬 ID 반환`() {
        // Given
        val keyword = "참이슬"
        val expectedResults = listOf(
            LiquorSearchResponse("liquor-001", "참이슬", "증류주", "하이트진로", "https://example.com/chamisul.jpg")
        )

        every { liquorSearchRepository.searchLiquors(keyword) } returns expectedResults

        // When
        val result = liquorSearchService.search(keyword)

        // Then
        assertEquals(1, result.size)
        assertEquals("liquor-001", result[0].id)
        assertEquals("참이슬", result[0].name)
        verify(exactly = 1) { liquorSearchRepository.searchLiquors(keyword) }
    }

    @Test
    fun `빈 키워드 검색시 빈 결과 반환`() {
        // Given
        val keyword = ""

        // When
        val autocompleteResult = liquorSearchService.autocomplete(keyword)
        val searchResult = liquorSearchService.search(keyword)

        // Then
        assertTrue(autocompleteResult.isEmpty())
        assertTrue(searchResult.isEmpty())
        verify(exactly = 0) { liquorSearchRepository.findLiquorsStartingWith(any()) }
        verify(exactly = 0) { liquorSearchRepository.searchLiquors(any()) }
    }

    @Test
    fun `자동완성 결과 제한 테스트`() {
        // Given
        val keyword = "ㅁ"
        val limit = 3
        val manyResults = (1..10).map {
            LiquorSearchResponse("liquor-$it", "막걸리$it", "탁주", "양조장$it", null)
        }

        every { liquorSearchRepository.findLiquorsStartingWith(keyword) } returns manyResults

        // When
        val result = liquorSearchService.autocomplete(keyword, limit)

        // Then
        assertEquals(3, result.size) // limit만큼만 반환되어야 함
        verify(exactly = 1) { liquorSearchRepository.findLiquorsStartingWith(keyword) }
    }
}
