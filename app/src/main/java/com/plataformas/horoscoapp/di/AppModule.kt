package com.plataformas.horoscoapp.di

import android.content.Context
import com.plataformas.horoscoapp.data.local.AppDatabase
import com.plataformas.horoscoapp.data.local.HoroscopeDao
import com.plataformas.horoscoapp.data.network.HoroscopeApiService
import com.plataformas.horoscoapp.data.network.NetworkMonitor
import com.plataformas.horoscoapp.data.network.NetworkModule
import com.plataformas.horoscoapp.data.preferences.NotificationPreferencesDataSource
import com.plataformas.horoscoapp.data.repository.HoroscopeRepository
import com.plataformas.horoscoapp.data.repository.NotificationRepository
import com.plataformas.horoscoapp.data.sync.HoroscopeSyncScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideHoroscopeDao(database: AppDatabase): HoroscopeDao {
        return database.horoscopeDao()
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }

    @Provides
    @Singleton
    fun provideHoroscopeApiService(): HoroscopeApiService {
        return NetworkModule.service
    }

    @Provides
    @Singleton
    fun provideHoroscopeRepository(
        dao: HoroscopeDao,
        networkMonitor: NetworkMonitor,
        api: HoroscopeApiService,
    ): HoroscopeRepository {
        return HoroscopeRepository(
            dao = dao,
            networkMonitor = networkMonitor,
            api = api,
        )
    }

    @Provides
    @Singleton
    fun provideNotificationPreferencesDataSource(
        @ApplicationContext context: Context,
    ): NotificationPreferencesDataSource {
        return NotificationPreferencesDataSource(context)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        preferences: NotificationPreferencesDataSource,
    ): NotificationRepository {
        return NotificationRepository(preferences)
    }

    @Provides
    @Singleton
    fun provideHoroscopeSyncScheduler(
        @ApplicationContext context: Context,
    ): HoroscopeSyncScheduler {
        return HoroscopeSyncScheduler(context)
    }
}
