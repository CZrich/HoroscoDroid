package com.plataformas.horoscoapp.data.repository

import com.plataformas.horoscoapp.data.local.HoroscopeDao
import com.plataformas.horoscoapp.data.local.HoroscopeEntity
import com.plataformas.horoscoapp.data.mapper.horoscopeCacheKey
import com.plataformas.horoscoapp.data.mapper.toEntity
import com.plataformas.horoscoapp.data.network.HoroscopeApiService
import com.plataformas.horoscoapp.data.network.NetworkMonitor
import com.plataformas.horoscoapp.data.network.NetworkModule
import com.plataformas.horoscoapp.core.model.HoroscopePeriod
import com.plataformas.horoscoapp.core.model.ZodiacSign
import java.io.IOException
import kotlinx.coroutines.flow.Flow

class HoroscopeRepository(
    private val dao: HoroscopeDao,
    private val networkMonitor: NetworkMonitor,
    private val api: HoroscopeApiService = NetworkModule.service,
) {
    fun observeHoroscope(sign: String, period: String): Flow<HoroscopeEntity?> {
        return dao.observeHoroscope(horoscopeCacheKey(sign, period))
    }

    suspend fun getCachedHoroscope(sign: String, period: String): HoroscopeEntity? {
        return dao.getHoroscope(horoscopeCacheKey(sign, period))
    }

    fun isOnline(): Boolean = networkMonitor.isOnline()

    fun observeNetwork(): Flow<Boolean> = networkMonitor.isOnlineFlow

    fun observeHistory(): Flow<List<HoroscopeEntity>> {
        return dao.observeHistory()
    }

    suspend fun syncHoroscope(sign: String, period: String) {
        if (sign == INVALID_SIGN) {
            throw IllegalArgumentException("Error: signo no existe")
        }

        if (!networkMonitor.isOnline()) {
            throw IOException("Sin conexión a Internet")
        }

        // Retrofit actualiza Room; la UI se refresca al observar la tabla local.
        val response = api.getHoroscope(period = period, sign = sign.lowercase())
        dao.upsertHoroscope(
            response.toEntity(
                requestedSign = sign,
                requestedPeriod = period,
            )
        )
    }

    suspend fun syncAvailableHoroscopes(
        signs: List<String> = AVAILABLE_SIGNS,
        periods: List<String> = AVAILABLE_PERIODS,
    ) {
        signs.forEach { sign ->
            periods.forEach { period ->
                syncHoroscope(sign = sign, period = period)
            }
        }
    }

    companion object {
        const val INVALID_SIGN = "Fall"

        val AVAILABLE_SIGNS = ZodiacSign.entries.map { sign -> sign.displayName }

        val AVAILABLE_PERIODS = HoroscopePeriod.entries.map { period -> period.apiValue }
    }
}
