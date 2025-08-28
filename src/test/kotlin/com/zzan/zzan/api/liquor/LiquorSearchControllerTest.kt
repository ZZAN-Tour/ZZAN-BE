
// src/test/kotlin/com/zzan/zzan/api/liquor/LiquorSearchControllerTest.kt (수정된 버전)
package com.zzan.zzan.api.liquor

import com.zzan.zzan.api.liquor.dto.LiquorSearchResponse
import com.zzan.zzan.common.annotation.UnitTest
import com.zzan.zzan.liquor.query.handler.LiquorSearchService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@UnitTest
@WebMvcTest(LiquorSearchController::class)
@WithMockUser
class LiquorSearchControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun mockLiquorSearchService(): LiquorSearchService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var liquorSearchService: LiquorSearchService

    @Test
    fun `자동완성 API 테스트 - 참 입력시 참으로 시작하는 전통주들 반환`() {
        // Given
        val keyword = "참"
        val autocompleteResults = listOf(
            LiquorSearchResponse("liquor-001", "참이슬", "증류주", "하이트진로", null),
            LiquorSearchResponse("liquor-002", "참참참", "탁주", "참좋은양조장", null)
        )

        every { liquorSearchService.autocomplete(keyword, 10) } returns autocompleteResults

        // When & Then
        mockMvc.perform(
            get("/api/liquors/autocomplete")
                .param("q", keyword)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].id").value("liquor-001"))
            .andExpect(jsonPath("$.data[0].name").value("참이슬"))
            .andExpect(jsonPath("$.data[1].name").value("참참참"))
    }

    @Test
    fun `정확한 검색 API 테스트 - 참이슬 검색시 참이슬 ID 반환`() {
        // Given
        val keyword = "참이슬"
        val searchResults = listOf(
            LiquorSearchResponse("liquor-001", "참이슬", "증류주", "하이트진로", "https://example.com/chamisul.jpg")
        )

        every { liquorSearchService.search(keyword) } returns searchResults

        // When & Then
        mockMvc.perform(
            get("/api/liquors/search")
                .param("q", keyword)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].id").value("liquor-001"))
            .andExpect(jsonPath("$.data[0].name").value("참이슬"))
    }

    @Test
    fun `자동완성 limit 파라미터 테스트`() {
        // Given
        val keyword = "ㅁ"
        val limit = 3
        val limitedResults = (1..3).map {
            LiquorSearchResponse("liquor-$it", "막걸리$it", "탁주", "양조장$it", null)
        }

        every { liquorSearchService.autocomplete(keyword, limit) } returns limitedResults

        // When & Then
        mockMvc.perform(
            get("/api/liquors/autocomplete")
                .param("q", keyword)
                .param("limit", limit.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.length()").value(3))
    }
}
