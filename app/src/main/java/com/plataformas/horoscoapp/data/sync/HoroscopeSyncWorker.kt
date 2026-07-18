package com.plataformas.horoscoapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.hilt.work.HiltWorker
import com.plataformas.horoscoapp.core.model.ZodiacSign
import com.plataformas.horoscoapp.data.repository.HoroscopeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

@HiltWorker
class HoroscopeSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: HoroscopeRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val sign = ZodiacSign.from(inputData.getString(KEY_SIGN)) ?: ZodiacSign.Aquarius
        val period = inputData.getString(KEY_PERIOD)?.takeIf { it.isNotBlank() } ?: DEFAULT_PERIOD

        return try {
            repository.syncHoroscope(sign = sign.displayName, period = period)
            Result.success(
                Data.Builder()
                    .putString(KEY_SYNCED_SIGN, sign.displayName)
                    .putString(KEY_SYNCED_PERIOD, period)
                    .putLong(KEY_SYNCED_AT, System.currentTimeMillis())
                    .build()
            )
        } catch (error: IOException) {
            Result.retry()
        } catch (error: IllegalArgumentException) {
            Result.failure()
        } catch (error: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "horoscope_periodic_sync"
        const val ONE_TIME_WORK_NAME = "horoscope_manual_sync"
        const val KEY_SIGN = "sign"
        const val KEY_PERIOD = "period"
        const val KEY_SYNCED_SIGN = "synced_sign"
        const val KEY_SYNCED_PERIOD = "synced_period"
        const val KEY_SYNCED_AT = "synced_at"
        const val DEFAULT_PERIOD = "daily"
    }
}
