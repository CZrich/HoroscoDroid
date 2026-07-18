package com.plataformas.horoscoapp.feature.tests

import android.content.Context
import com.plataformas.horoscoapp.app.TestNotificationType
import com.plataformas.horoscoapp.data.notification.DailyHoroscopeNotifier
import com.plataformas.horoscoapp.data.notification.NotificationConstants.ROUTE_PREFIX
import java.util.Locale

fun showTestNotification(
    context: Context,
    type: TestNotificationType,
    sign: String,
) {
    val route = "$ROUTE_PREFIX${sign.lowercase(Locale.US)}"
    val message = "Descubre lo que los astros tienen preparado hoy para $sign."
    val notifier = DailyHoroscopeNotifier(context)

    when (type) {
        TestNotificationType.Data -> notifier.showDailyHoroscopeNotification(
            title = "Tu horóscopo diario de $sign",
            message = message,
            sign = sign,
            route = route,
        )
        TestNotificationType.Notification -> notifier.showDailyHoroscopeNotification(
            title = "Tu horóscopo de hoy",
            message = "Revisa lo que los astros tienen preparado para ti.",
            sign = sign,
            route = route,
        )
    }
}
