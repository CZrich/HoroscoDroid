package com.plataformas.horoscoapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.plataformas.horoscoapp.di.AppContainer

class HoroscopeSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Background sync keeps Room fresh; Compose observes Room and updates itself.
            AppContainer.horoscopeRepository(applicationContext)
                .syncAvailableHoroscopes()
            Result.success()
        } catch (error: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "horoscope_periodic_sync"
    }
}
