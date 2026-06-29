package com.plataformas.horoscoapp.ui.horoscope

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plataformas.horoscoapp.data.repository.HoroscopeRepository
import com.plataformas.horoscoapp.ui.state.HoroscopeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HoroscopeViewModel(
    private val repository: HoroscopeRepository = HoroscopeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HoroscopeUiState>(HoroscopeUiState.Loading)
    val uiState: StateFlow<HoroscopeUiState> = _uiState

    private val _selectedSign = MutableStateFlow("Aquarius")
    val selectedSign: StateFlow<String> = _selectedSign

    private val _selectedPeriod = MutableStateFlow("daily")
    val selectedPeriod: StateFlow<String> = _selectedPeriod

    val availableSigns = listOf(
        "Aries",
        "Taurus",
        "Gemini",
        "Cancer",
        "Leo",
        "Virgo",
        "Libra",
        "Scorpio",
        "Sagittarius",
        "Capricorn",
        "Aquarius",
        "Pisces",
    )

    val availablePeriods = listOf("daily", "weekly", "monthly")

    init {
        fetchHoroscope()
    }

    fun setSign(sign: String) {
        if (sign == _selectedSign.value) return
        _selectedSign.value = sign
        fetchHoroscope()
    }

    fun setPeriod(period: String) {
        if (period == _selectedPeriod.value) return
        _selectedPeriod.value = period
        fetchHoroscope()
    }

    fun fetchHoroscope() {
        viewModelScope.launch {
            _uiState.value = HoroscopeUiState.Loading
            try {
                val response = repository.fetchHoroscope(
                    sign = _selectedSign.value,
                    period = _selectedPeriod.value,
                )
                _uiState.value = HoroscopeUiState.Success(response.data)
            } catch (error: Exception) {
                _uiState.value = HoroscopeUiState.Error(
                    error.localizedMessage ?: "No se pudo cargar el horóscopo"
                )
            }
        }
    }
}
