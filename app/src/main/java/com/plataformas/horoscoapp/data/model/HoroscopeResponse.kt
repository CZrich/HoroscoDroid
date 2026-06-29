package com.plataformas.horoscoapp.data.model

data class HoroscopeResponse(
    val data: HoroscopeData
)

data class HoroscopeData(
    val date: String,
    val period: String,
    val sign: String,
    val horoscope: String
)
