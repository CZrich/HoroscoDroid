package com.plataformas.horoscoapp.messaging

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.plataformas.horoscoapp.data.notification.DailyHoroscopeNotifier

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
        val payload = HoroscopeMessagePayloadParser.parse(data)
        if (payload == null) {
            Log.w(TAG, "Invalid or unsupported FCM data message: $data")
            return
        }

        DailyHoroscopeNotifier(this).showDailyHoroscopeNotification(
            title = payload.title,
            message = payload.message,
            sign = payload.sign,
            route = payload.route,
        )
    }
    private companion object {
        const val TAG = "HoroscopeMessaging"
    }
}
