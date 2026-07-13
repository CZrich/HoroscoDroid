package com.plataformas.horoscoapp.ui.horoscope

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.plataformas.horoscoapp.data.mapper.toDomain
import com.plataformas.horoscoapp.data.repository.HoroscopeRepository
import com.plataformas.horoscoapp.data.repository.NotificationRepository
import com.plataformas.horoscoapp.di.AppContainer
import com.plataformas.horoscoapp.ui.state.HoroscopeUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HoroscopeViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val repository = AppContainer.horoscopeRepository(application)
    private val notificationRepository = AppContainer.notificationRepository(application)

    private val _uiState = MutableStateFlow<HoroscopeUiState>(HoroscopeUiState.Loading)
    val uiState: StateFlow<HoroscopeUiState> = _uiState

    private val _selectedSign = MutableStateFlow("Aquarius")
    val selectedSign: StateFlow<String> = _selectedSign

    private val _selectedPeriod = MutableStateFlow("daily")
    val selectedPeriod: StateFlow<String> = _selectedPeriod

    private val _selectedNotificationSign = MutableStateFlow<String?>(null)
    val selectedNotificationSign: StateFlow<String?> = _selectedNotificationSign

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage

    val availableSigns = HoroscopeRepository.AVAILABLE_SIGNS + HoroscopeRepository.INVALID_SIGN

    val availableNotificationSigns = HoroscopeRepository.AVAILABLE_SIGNS

    val availablePeriods = HoroscopeRepository.AVAILABLE_PERIODS

    private var cacheJob: Job? = null

    init {
        observeCachedHoroscope()
        observeNetworkRecovery()
        observeSelectedNotificationSign()
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
            val sign = _selectedSign.value
            val period = _selectedPeriod.value
            val cached = repository.getCachedHoroscope(sign, period)

            if (cached == null) {
                _uiState.value = HoroscopeUiState.Loading
            }

            if (sign == HoroscopeRepository.INVALID_SIGN) {
                _uiState.value = HoroscopeUiState.Error("Error: signo no existe")
                return@launch
            }

            try {
                repository.syncHoroscope(sign = sign, period = period)
            } catch (error: Exception) {
                if (cached == null) {
                    _uiState.value = HoroscopeUiState.Error(
                        error.localizedMessage ?: "No se pudo cargar el horóscopo"
                    )
                }
            }
        }
    }

    fun openSignFromNotification(sign: String) {
        val projectSign = sign.toProjectSign() ?: return
        _selectedPeriod.value = "daily"
        if (projectSign != _selectedSign.value) {
            _selectedSign.value = projectSign
        }
        fetchHoroscope()
    }

    fun selectNotificationSign(sign: String) {
        val projectSign = sign.toProjectSign()
        if (projectSign == null) {
            _notificationMessage.value = "No se pudo activar: signo inválido."
            return
        }

        viewModelScope.launch {
            _notificationMessage.value = "Activando notificaciones para $projectSign..."
            notificationRepository.selectSign(projectSign)
                .onSuccess {
                    _selectedNotificationSign.value = projectSign
                    _notificationMessage.value = "Notificaciones activadas para $projectSign."
                }
                .onFailure { error ->
                    _notificationMessage.value = error.localizedMessage
                        ?: "No se pudo activar el topic de notificaciones."
                }
        }
    }

    fun onNotificationPermissionDenied() {
        _notificationMessage.value = "Permiso de notificaciones denegado. Podés habilitarlo desde ajustes del sistema."
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCachedHoroscope() {
        cacheJob?.cancel()
        cacheJob = viewModelScope.launch {
            combine(
                _selectedSign,
                _selectedPeriod,
            ) { sign, period -> sign to period }
                .distinctUntilChanged()
                .flatMapLatest { (sign, period) ->
                    repository.observeHoroscope(sign, period).map { entity -> sign to entity }
                }
                .collectLatest { (sign, entity) ->
                    if (sign == HoroscopeRepository.INVALID_SIGN) return@collectLatest
                    entity ?: return@collectLatest

                    _uiState.value = HoroscopeUiState.Success(
                        horoscope = entity.toDomain(),
                        isCached = !repository.isOnline(),
                        updatedAt = entity.updatedAt,
                    )
                }
        }
    }

    private fun observeNetworkRecovery() {
        viewModelScope.launch {
            repository.observeNetwork()
                .filter { isOnline -> isOnline }
                .collectLatest {
                    fetchHoroscope()
                }
        }
    }

    private fun observeSelectedNotificationSign() {
        viewModelScope.launch {
            notificationRepository.selectedSign.collectLatest { sign ->
                _selectedNotificationSign.value = sign
            }
        }
    }

    private fun String.toProjectSign(): String? {
        return HoroscopeRepository.AVAILABLE_SIGNS.firstOrNull { sign ->
            sign.equals(this, ignoreCase = true)
        }
    }
}
