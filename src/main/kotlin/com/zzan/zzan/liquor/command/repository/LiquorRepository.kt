package com.zzan.zzan.liquor.command.repository

import com.zzan.zzan.liquor.command.domain.Liquor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface LiquorRepository : JpaRepository<Liquor, String> {

    @Query("SELECT l FROM Liquor l WHERE l.name LIKE %:name%")
    fun findByNameContaining(name: String): List<Liquor>

    @Query("SELECT l FROM Liquor l WHERE l.brewery = :brewery")
    fun findByBrewery(brewery: String): List<Liquor>

    @Query("SELECT l FROM Liquor l WHERE l.type = :type")
    fun findByType(type: String): List<Liquor>
}
