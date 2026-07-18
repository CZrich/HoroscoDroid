package com.plataformas.horoscoapp

import com.plataformas.horoscoapp.core.model.ZodiacSign
import com.plataformas.horoscoapp.data.repository.NotificationRepository
import com.plataformas.horoscoapp.messaging.HoroscopeMessagePayloadParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AstroDailyUnitTest {
    @Test
    fun zodiacSignParsesDisplayNameAndApiValue() {
        assertEquals(ZodiacSign.Aries, ZodiacSign.from("Aries"))
        assertEquals(ZodiacSign.Aries, ZodiacSign.from("aries"))
    }

    @Test
    fun topicForSignUsesStableFirebaseTopicFormat() {
        assertEquals("horoscope_aries", NotificationRepository.topicForSign("Aries"))
        assertEquals("horoscope_sagittarius", NotificationRepository.topicForSign("Sagittarius"))
    }

    @Test
    fun dataMessagePayloadBuildsDefaultRoute() {
        val payload = HoroscopeMessagePayloadParser.parse(
            mapOf(
                "type" to "daily_horoscope",
                "sign" to "leo",
                "title" to "Tu horóscopo de hoy",
                "message" to "Descubre tu lectura diaria.",
            )
        )

        assertEquals("Leo", payload?.sign)
        assertEquals("horoscope/leo", payload?.route)
    }

    @Test
    fun dataMessagePayloadRejectsUnknownTypeAndInvalidSign() {
        assertNull(HoroscopeMessagePayloadParser.parse(mapOf("type" to "other", "sign" to "aries")))
        assertNull(HoroscopeMessagePayloadParser.parse(mapOf("type" to "daily_horoscope", "sign" to "ophiuchus")))
    }
}
