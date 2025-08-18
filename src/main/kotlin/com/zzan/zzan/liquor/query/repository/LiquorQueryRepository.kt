package com.zzan.zzan.liquor.query.repository

import com.zzan.zzan.api.liquor.dto.LiquorDetailResponse
import com.zzan.zzan.liquor.command.domain.Liquor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface LiquorQueryRepository : JpaRepository<Liquor, String> {

    @Query(
        """
        SELECT new com.zzan.zzan.api.liquor.dto.LiquorDetailResponse(
            l.id, l.name, l.type, l.score, l.description,
            l.foodPairing, l.volume, l.content, l.awards,
            l.etc, l.imageUrl, l.brewery
        )
        FROM Liquor l WHERE l.id = :id
        """
    )
    fun getLiquorById(id: String): LiquorDetailResponse?
}
