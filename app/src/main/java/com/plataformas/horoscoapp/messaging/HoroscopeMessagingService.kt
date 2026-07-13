package com.plataformas.horoscoapp.messaging

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.plataformas.horoscoapp.data.notification.DailyHoroscopeNotifier
import com.plataformas.horoscoapp.data.notification.NotificationConstants.ROUTE_PREFIX
import com.plataformas.horoscoapp.data.notification.NotificationConstants.TYPE_DAILY_HOROSCOPE
import com.plataformas.horoscoapp.data.repository.HoroscopeRepository
import java.util.Locale

class HoroscopeMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            handleDataMessage(data)
            return
        }

        val notification = remoteMessage.notification
        val title = notification?.title
        val body = notification?.body
        if (title.isNullOrBlank() && body.isNullOrBlank()) {
            Log.w(TAG, "Notification message without title or body")
            return
        }

        DailyHoroscopeNotifier(this).showDailyHoroscopeNotification(
            title = title,
            message = body,
            sign = null,
            route = null,
        )
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        if (type != TYPE_DAILY_HOROSCOPE) {
            Log.w(TAG, "Unknown FCM data message type: $type")
            return
        }

        val sign = data["sign"]?.toProjectSign()
        if (sign == null) {
            Log.w(TAG, "Daily horoscope data message without a valid sign: ${data["sign"]}")
            return
        }

        val route = data["route"]?.takeIf { it.isNotBlank() } ?: "$ROUTE_PREFIX${sign.lowercase(Locale.US)}"
        if (!route.startsWith(ROUTE_PREFIX)) {
            Log.w(TAG, "Daily horoscope data message with unexpected route: $route")
        }

        DailyHoroscopeNotifier(this).showDailyHoroscopeNotification(
            title = data["title"],
            message = data["message"],
            sign = sign,
            route = route,
        )
    }

    private fun String.toProjectSign(): String? {
        return HoroscopeRepository.AVAILABLE_SIGNS.firstOrNull { sign ->
            sign.equals(this, ignoreCase = true)
        }
    }

    private companion object {
        const val TAG = "HoroscopeMessaging"
    }
}
