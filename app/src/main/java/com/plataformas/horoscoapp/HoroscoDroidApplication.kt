package com.plataformas.horoscoapp

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.workDataOf
import androidx.work.WorkManager
import com.plataformas.horoscoapp.core.model.ZodiacSign
import com.plataformas.horoscoapp.data.notification.DailyHoroscopeNotifier
import com.plataformas.horoscoapp.data.sync.HoroscopeSyncWorker
import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class HoroscoDroidApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        DailyHoroscopeNotifier(this).createNotificationChannel()
        scheduleHoroscopeSync()
    }

    private fun scheduleHoroscopeSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<HoroscopeSyncWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS,
            )
            .setInputData(
                workDataOf(
                    HoroscopeSyncWorker.KEY_SIGN to ZodiacSign.Aquarius.apiValue,
                    HoroscopeSyncWorker.KEY_PERIOD to HoroscopeSyncWorker.DEFAULT_PERIOD,
                )
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            HoroscopeSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
