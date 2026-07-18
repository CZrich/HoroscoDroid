package com.plataformas.horoscoapp.messaging

import com.plataformas.horoscoapp.core.model.ZodiacSign
import com.plataformas.horoscoapp.data.notification.NotificationConstants.ROUTE_PREFIX
import com.plataformas.horoscoapp.data.notification.NotificationConstants.TYPE_DAILY_HOROSCOPE

data class HoroscopeMessagePayload(
    val title: String?,
    val message: String?,
    val sign: String,
    val route: String,
)

object HoroscopeMessagePayloadParser {
    fun parse(data: Map<String, String>): HoroscopeMessagePayload? {
        if (data["type"] != TYPE_DAILY_HOROSCOPE) return null

        val sign = ZodiacSign.from(data["sign"]) ?: return null
        val route = data["route"]?.takeIf { it.isNotBlank() } ?: sign.route
        if (!route.startsWith(ROUTE_PREFIX)) return null

        return HoroscopeMessagePayload(
            title = data["title"],
            message = data["message"],
            sign = sign.displayName,
            route = route,
        )
    }
}
