package com.plataformas.horoscoapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "horoscopes")
data class HoroscopeEntity(
    @PrimaryKey val cacheKey: String,
    val sign: String,
    val period: String,
    val date: String,
    val horoscope: String,
    val updatedAt: Long,
)
