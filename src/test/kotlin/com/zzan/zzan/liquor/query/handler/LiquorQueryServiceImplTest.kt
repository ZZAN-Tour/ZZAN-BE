package com.zzan.zzan.liquor.query.handler

import com.zzan.zzan.api.liquor.dto.LiquorDetailResponse
import com.zzan.zzan.common.annotation.UnitTest
import com.zzan.zzan.common.exception.CustomException
import com.zzan.zzan.liquor.query.repository.LiquorQueryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnitTest
class LiquorQueryServiceImplTest {

    private val liquorQueryRepository = mockk<LiquorQueryRepository>()
    private val liquorQueryService = LiquorQueryServiceImpl(liquorQueryRepository)

    @Test
    fun `술 조회 성공 테스트`() {
        // Given
        val liquorId = "test-liquor-id"
        val expectedResponse = LiquorDetailResponse(
            id = liquorId,
            name = "테스트 소주",
            type = "증류주",
            score = null,
            description = "깔끔한 맛의 소주",
            foodPairing = "삼겹살, 치킨",
            volume = "360ml",
            content = "17%",
            awards = "2024년 우수상",
            etc = "무감미료",
            imageUrl = "https://example.com/image.jpg",
            brewery = "테스트 양조장"
        )

        every { liquorQueryRepository.getLiquorById(liquorId) } returns expectedResponse

        // When
        val result = liquorQueryService.getLiquorById(liquorId)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.name, result.name)
        assertEquals(expectedResponse.type, result.type)
        assertEquals(expectedResponse.description, result.description)
        assertEquals(expectedResponse.brewery, result.brewery)
        verify(exactly = 1) { liquorQueryRepository.getLiquorById(liquorId) }
    }

    @Test
    fun `존재하지 않는 술 조회시 CustomException 발생 테스트`() {
        // Given
        val nonExistentId = "non-existent-id"
        every { liquorQueryRepository.getLiquorById(nonExistentId) } returns null

        // When & Then
        val exception = assertThrows<CustomException> {
            liquorQueryService.getLiquorById(nonExistentId)
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.status)
        verify(exactly = 1) { liquorQueryRepository.getLiquorById(nonExistentId) }
    }
}
