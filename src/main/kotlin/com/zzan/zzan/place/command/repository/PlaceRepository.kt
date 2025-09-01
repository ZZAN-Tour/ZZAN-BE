package com.zzan.zzan.place.command.repository

import com.zzan.zzan.place.command.domain.Place
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlaceRepository : JpaRepository<Place, String>
