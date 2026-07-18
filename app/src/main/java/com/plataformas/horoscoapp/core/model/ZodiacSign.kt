package com.plataformas.horoscoapp.core.model

import java.text.Normalizer
import java.util.Locale

private const val ZODIAC_TOPIC_PREFIX = "horoscope_"

enum class ZodiacSign(
    val apiValue: String,
    val displayName: String,
    val symbol: String,
) {
    Aries("aries", "Aries", "♈"),
    Taurus("taurus", "Taurus", "♉"),
    Gemini("gemini", "Gemini", "♊"),
    Cancer("cancer", "Cancer", "♋"),
    Leo("leo", "Leo", "♌"),
    Virgo("virgo", "Virgo", "♍"),
    Libra("libra", "Libra", "♎"),
    Scorpio("scorpio", "Scorpio", "♏"),
    Sagittarius("sagittarius", "Sagittarius", "♐"),
    Capricorn("capricorn", "Capricorn", "♑"),
    Aquarius("aquarius", "Aquarius", "♒"),
    Pisces("pisces", "Pisces", "♓");

    val topicName: String = ZODIAC_TOPIC_PREFIX + apiValue
    val route: String = "horoscope/$apiValue"

    companion object {
        fun from(value: String?): ZodiacSign? {
            val normalized = value?.normalizeIdentifier() ?: return null
            return entries.firstOrNull { sign ->
                sign.apiValue == normalized || sign.displayName.normalizeIdentifier() == normalized
            }
        }
    }
}

fun String.normalizeIdentifier(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase(Locale.US)
        .replace("[^a-z0-9-_.~%]+".toRegex(), "_")
        .trim('_')
}
