package com.plataformas.horoscoapp.data.repository

import com.plataformas.horoscoapp.data.model.HoroscopeResponse
import com.plataformas.horoscoapp.data.network.HoroscopeApiService
import com.plataformas.horoscoapp.data.network.NetworkModule

class HoroscopeRepository(
    private val api: HoroscopeApiService = NetworkModule.service,
) {
    suspend fun fetchHoroscope(sign: String, period: String): HoroscopeResponse {
        return api.getHoroscope(period = period, sign = sign.lowercase())
    }
}
