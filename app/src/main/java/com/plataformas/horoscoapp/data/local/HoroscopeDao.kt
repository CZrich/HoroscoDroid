package com.plataformas.horoscoapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HoroscopeDao {
    @Query("SELECT * FROM horoscopes WHERE cacheKey = :cacheKey LIMIT 1")
    fun observeHoroscope(cacheKey: String): Flow<HoroscopeEntity?>

    @Query("SELECT * FROM horoscopes WHERE sign = :sign ORDER BY updatedAt DESC")
    fun observeBySign(sign: String): Flow<List<HoroscopeEntity>>

    @Query("SELECT * FROM horoscopes ORDER BY updatedAt DESC")
    fun observeHistory(): Flow<List<HoroscopeEntity>>

    @Query("SELECT * FROM horoscopes WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getHoroscope(cacheKey: String): HoroscopeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHoroscope(horoscope: HoroscopeEntity)
}
