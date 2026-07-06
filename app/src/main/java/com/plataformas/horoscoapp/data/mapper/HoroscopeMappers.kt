package com.plataformas.horoscoapp.data.mapper

import com.plataformas.horoscoapp.data.local.HoroscopeEntity
import com.plataformas.horoscoapp.data.model.HoroscopeData
import com.plataformas.horoscoapp.data.model.HoroscopeResponse

fun horoscopeCacheKey(sign: String, period: String): String {
    return "${sign.trim().lowercase()}_${period.trim().lowercase()}"
}

fun HoroscopeResponse.toEntity(
    requestedSign: String,
    requestedPeriod: String,
    updatedAt: Long = System.currentTimeMillis(),
): HoroscopeEntity {
    return HoroscopeEntity(
        cacheKey = horoscopeCacheKey(requestedSign, requestedPeriod),
        sign = data.sign.ifBlank { requestedSign },
        period = data.period.ifBlank { requestedPeriod },
        date = data.date,
        horoscope = data.horoscope,
        updatedAt = updatedAt,
    )
}

fun HoroscopeEntity.toDomain(): HoroscopeData {
    return HoroscopeData(
        date = date,
        period = period,
        sign = sign,
        horoscope = horoscope,
    )
}
