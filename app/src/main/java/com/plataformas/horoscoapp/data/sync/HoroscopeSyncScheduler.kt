package com.plataformas.horoscoapp.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HoroscopeSyncScheduler(
    context: Context,
) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    fun enqueuePeriodicSync(sign: String, period: String = HoroscopeSyncWorker.DEFAULT_PERIOD) {
        val request = PeriodicWorkRequestBuilder<HoroscopeSyncWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS,
            )
            .setInputData(
                workDataOf(
                    HoroscopeSyncWorker.KEY_SIGN to sign,
                    HoroscopeSyncWorker.KEY_PERIOD to period,
                )
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            HoroscopeSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun enqueueManualSync(sign: String, period: String = HoroscopeSyncWorker.DEFAULT_PERIOD) {
        val request = OneTimeWorkRequestBuilder<HoroscopeSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS,
            )
            .setInputData(
                workDataOf(
                    HoroscopeSyncWorker.KEY_SIGN to sign,
                    HoroscopeSyncWorker.KEY_PERIOD to period,
                )
            )
            .build()

        workManager.enqueueUniqueWork(
            HoroscopeSyncWorker.ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun observeManualSync(): Flow<WorkInfo?> {
        return workManager.getWorkInfosForUniqueWorkFlow(HoroscopeSyncWorker.ONE_TIME_WORK_NAME)
            .map { workInfos -> workInfos.firstOrNull() }
    }
}
