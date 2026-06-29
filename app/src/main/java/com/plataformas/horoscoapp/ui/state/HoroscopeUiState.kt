package com.plataformas.horoscoapp.ui.state

import com.plataformas.horoscoapp.data.model.HoroscopeData

sealed interface HoroscopeUiState {
    object Loading : HoroscopeUiState
    data class Success(val horoscope: HoroscopeData) : HoroscopeUiState
    data class Error(val message: String) : HoroscopeUiState
}
