package com.plataformas.horoscoapp.data.network

import com.plataformas.horoscoapp.data.model.HoroscopeResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HoroscopeApiService {
    @GET("api/v1/get-horoscope/{period}")
    suspend fun getHoroscope(
        @Path("period") period: String,
        @Query("sign") sign: String,
    ): HoroscopeResponse
}
