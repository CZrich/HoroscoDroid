package com.plataformas.horoscoapp

import com.plataformas.horoscoapp.data.local.HoroscopeEntity
import com.plataformas.horoscoapp.data.mapper.horoscopeCacheKey
import com.plataformas.horoscoapp.data.mapper.toDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class HoroscopeMapperTest {
    @Test
    fun cacheKeyNormalizesSignAndPeriod() {
        assertEquals("leo_daily", horoscopeCacheKey(" Leo ", " Daily "))
    }

    @Test
    fun entityMapsToDomainModel() {
        val domain = HoroscopeEntity(
            cacheKey = "aries_daily",
            sign = "Aries",
            period = "daily",
            date = "2026-07-17",
            horoscope = "Test horoscope",
            updatedAt = 123L,
        ).toDomain()

        assertEquals("Aries", domain.sign)
        assertEquals("daily", domain.period)
        assertEquals("Test horoscope", domain.horoscope)
    }
}
