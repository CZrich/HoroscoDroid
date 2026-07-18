package com.plataformas.horoscoapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.plataformas.horoscoapp.data.local.AppDatabase
import com.plataformas.horoscoapp.data.local.HoroscopeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HoroscopeDaoTest {
    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun upsertAndObserveHistoryReturnsLatestFirst() = runBlocking {
        val dao = database.horoscopeDao()
        dao.upsertHoroscope(entity("aries_daily", "Aries", 100L))
        dao.upsertHoroscope(entity("leo_daily", "Leo", 200L))

        val history = dao.observeHistory().first()

        assertEquals(listOf("Leo", "Aries"), history.map { it.sign })
    }

    private fun entity(cacheKey: String, sign: String, updatedAt: Long): HoroscopeEntity {
        return HoroscopeEntity(
            cacheKey = cacheKey,
            sign = sign,
            period = "daily",
            date = "2026-07-17",
            horoscope = "Content for $sign",
            updatedAt = updatedAt,
        )
    }
}
