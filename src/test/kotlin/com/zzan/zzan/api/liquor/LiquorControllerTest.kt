package com.zzan.zzan.api.liquor

import com.zzan.zzan.api.liquor.dto.LiquorDetailResponse
import com.zzan.zzan.common.annotation.UnitTest
import com.zzan.zzan.common.exception.CustomException
import com.zzan.zzan.liquor.query.handler.LiquorQueryService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@UnitTest
@WebMvcTest(LiquorController::class)
@WithMockUser
class LiquorControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun mockLiquorQueryService(): LiquorQueryService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var liquorQueryService: LiquorQueryService

    @Test
    fun `술 상세 조회 API 성공 테스트`() {
        // Given
        val liquorId = "test-liquor-123"
        val liquorResponse = LiquorDetailResponse(
            id = liquorId,
            name = "참이슬",
            type = "희석식 소주",
            score = null,
            description = "깔끔하고 부드러운 맛",
            foodPairing = "삼겹살, 회",
            volume = "360ml",
            content = "16.9%",
            awards = "2023년 소주 대상",
            etc = "순곡 소주",
            imageUrl = "https://example.com/chamisul.jpg",
            brewery = "하이트진로"
        )

        every { liquorQueryService.getLiquorById(liquorId) } returns liquorResponse

        // When & Then
        mockMvc.perform(
            get("/liquors/{id}", liquorId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(liquorId))
            .andExpect(jsonPath("$.data.name").value("참이슬"))
            .andExpect(jsonPath("$.data.type").value("희석식 소주"))
            .andExpect(jsonPath("$.data.description").value("깔끔하고 부드러운 맛"))
            .andExpect(jsonPath("$.data.brewery").value("하이트진로"))
            .andExpect(jsonPath("$.data.volume").value("360ml"))
            .andExpect(jsonPath("$.data.content").value("16.9%"))
            .andExpect(jsonPath("$.timestamp").isNumber)
            .andExpect(jsonPath("$.message").doesNotExist())

        verify(exactly = 1) { liquorQueryService.getLiquorById(liquorId) }
    }

    @Test
    fun `존재하지 않는 술 조회 API 실패 테스트`() {
        // Given
        val nonExistentId = "non-existent-123"
        every { liquorQueryService.getLiquorById(nonExistentId) } throws CustomException(
            status = HttpStatus.NOT_FOUND,
            message = "술을 찾을 수 없습니다"
        )

        // When & Then
        mockMvc.perform(
            get("/liquors/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)

        verify(exactly = 1) { liquorQueryService.getLiquorById(nonExistentId) }
    }


    @Test
    fun `서비스에서 예외 발생시 글로벌 예외 처리 테스트`() {
        // Given
        val liquorId = "error-liquor"
        every { liquorQueryService.getLiquorById(liquorId) } throws CustomException(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
        )

        // When & Then
        mockMvc.perform(
            get("/liquors/{id}", liquorId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isInternalServerError)

        verify(exactly = 1) { liquorQueryService.getLiquorById(liquorId) }
    }
}
