// src/main/kotlin/com/zzan/zzan/liquor/query/repository/LiquorSearchRepository.kt (수정)
package com.zzan.zzan.liquor.query.repository

import com.zzan.zzan.api.liquor.dto.LiquorSearchResponse
import com.zzan.zzan.liquor.command.domain.Liquor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LiquorSearchRepository : JpaRepository<Liquor, String> {

    /**
     * 자동완성용 전통주 검색
     * 입력한 키워드로 시작하는 전통주들을 반환
     */
    @Query("""
        SELECT new com.zzan.zzan.api.liquor.dto.LiquorSearchResponse(
            l.id, l.name, l.type, l.brewery, l.imageUrl
        )
        FROM Liquor l 
        WHERE LOWER(l.name) LIKE LOWER(CONCAT(:keyword, '%'))
        ORDER BY l.name
    """)
    fun findLiquorsStartingWith(@Param("keyword") keyword: String): List<LiquorSearchResponse>

    /**
     * 전체 텍스트 검색 (이름, 타입, 양조장명 포함)
     */
    @Query("""
        SELECT new com.zzan.zzan.api.liquor.dto.LiquorSearchResponse(
            l.id, l.name, l.type, l.brewery, l.imageUrl
        )
        FROM Liquor l 
        WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
              LOWER(l.type) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
              LOWER(l.brewery) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY 
            CASE WHEN LOWER(l.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1 ELSE 2 END,
            l.name
    """)
    fun searchLiquors(@Param("keyword") keyword: String): List<LiquorSearchResponse>
}
