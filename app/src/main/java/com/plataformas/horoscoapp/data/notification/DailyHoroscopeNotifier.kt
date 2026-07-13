package com.plataformas.horoscoapp.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.plataformas.horoscoapp.MainActivity
import com.plataformas.horoscoapp.R
import com.plataformas.horoscoapp.data.notification.NotificationConstants.DAILY_HOROSCOPE_CHANNEL_ID
import com.plataformas.horoscoapp.data.notification.NotificationConstants.EXTRA_ROUTE
import com.plataformas.horoscoapp.data.notification.NotificationConstants.EXTRA_SIGN

class DailyHoroscopeNotifier(
    private val context: Context,
) {
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            DAILY_HOROSCOPE_CHANNEL_ID,
            "Horóscopo diario",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alertas diarias para consultar tu horóscopo."
            enableVibration(true)
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showDailyHoroscopeNotification(
        title: String?,
        message: String?,
        sign: String?,
        route: String?,
    ) {
        val safeTitle = title?.takeIf { it.isNotBlank() } ?: "Tu horóscopo de hoy"
        val safeMessage = message?.takeIf { it.isNotBlank() }
            ?: sign?.takeIf { it.isNotBlank() }?.let {
                "Descubre lo que los astros tienen preparado hoy para ${it.replaceFirstChar { char -> char.uppercase() }}."
            }
            ?: "Revisa lo que los astros tienen preparado para ti."

        if (!canPostNotifications()) {
            Log.w(TAG, "Notification permission denied; daily horoscope notification was not shown")
            return
        }

        createNotificationChannel()

        val pendingIntent = PendingIntent.getActivity(
            context,
            (route ?: sign ?: safeMessage).hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_SIGN, sign)
                putExtra(EXTRA_ROUTE, route)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, DAILY_HOROSCOPE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_horoscope)
            .setContentTitle(safeTitle)
            .setContentText(safeMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(safeMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify((route ?: sign ?: safeTitle).hashCode(), notification)
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val TAG = "DailyHoroscopeNotifier"
    }
}
