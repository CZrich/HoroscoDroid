package com.plataformas.horoscoapp.di

import android.content.Context
import com.plataformas.horoscoapp.data.local.AppDatabase
import com.plataformas.horoscoapp.data.network.NetworkMonitor
import com.plataformas.horoscoapp.data.network.NetworkModule
import com.plataformas.horoscoapp.data.preferences.NotificationPreferencesDataSource
import com.plataformas.horoscoapp.data.repository.HoroscopeRepository
import com.plataformas.horoscoapp.data.repository.NotificationRepository

object AppContainer {
    @Volatile
    private var repository: HoroscopeRepository? = null

    @Volatile
    private var notificationRepository: NotificationRepository? = null

    fun horoscopeRepository(context: Context): HoroscopeRepository {
        return repository ?: synchronized(this) {
            repository ?: HoroscopeRepository(
                dao = AppDatabase.getInstance(context).horoscopeDao(),
                networkMonitor = NetworkMonitor(context.applicationContext),
                api = NetworkModule.service,
            ).also { repository = it }
        }
    }

    fun notificationRepository(context: Context): NotificationRepository {
        return notificationRepository ?: synchronized(this) {
            notificationRepository ?: NotificationRepository(
                preferences = NotificationPreferencesDataSource(context.applicationContext),
            ).also { notificationRepository = it }
        }
    }
}
