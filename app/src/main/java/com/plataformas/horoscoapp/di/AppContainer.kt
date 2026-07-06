package com.plataformas.horoscoapp.di

import android.content.Context
import com.plataformas.horoscoapp.data.local.AppDatabase
import com.plataformas.horoscoapp.data.network.NetworkMonitor
import com.plataformas.horoscoapp.data.network.NetworkModule
import com.plataformas.horoscoapp.data.repository.HoroscopeRepository

object AppContainer {
    @Volatile
    private var repository: HoroscopeRepository? = null

    fun horoscopeRepository(context: Context): HoroscopeRepository {
        return repository ?: synchronized(this) {
            repository ?: HoroscopeRepository(
                dao = AppDatabase.getInstance(context).horoscopeDao(),
                networkMonitor = NetworkMonitor(context.applicationContext),
                api = NetworkModule.service,
            ).also { repository = it }
        }
    }
}
