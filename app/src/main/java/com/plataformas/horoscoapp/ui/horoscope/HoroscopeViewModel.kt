package com.plataformas.horoscoapp.feature.horoscope

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.plataformas.horoscoapp.data.mapper.toDomain
import com.plataformas.horoscoapp.data.model.HoroscopeData
import com.plataformas.horoscoapp.data.repository.HoroscopeRepository
import com.plataformas.horoscoapp.data.repository.NotificationRepository
import com.plataformas.horoscoapp.data.sync.HoroscopeSyncWorker
import com.plataformas.horoscoapp.ui.state.HoroscopeUiState
import com.plataformas.horoscoapp.data.sync.HoroscopeSyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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

@HiltViewModel
class HoroscopeViewModel @Inject constructor(
    private val repository: HoroscopeRepository,
    private val notificationRepository: NotificationRepository,
    private val syncScheduler: HoroscopeSyncScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HoroscopeUiState>(HoroscopeUiState.Loading)
    val uiState: StateFlow<HoroscopeUiState> = _uiState

    private val _selectedSign = MutableStateFlow("Aquarius")
    val selectedSign: StateFlow<String> = _selectedSign

    private val _selectedPeriod = MutableStateFlow("daily")
    val selectedPeriod: StateFlow<String> = _selectedPeriod

    private val _selectedNotificationSign = MutableStateFlow<String?>(null)
    val selectedNotificationSign: StateFlow<String?> = _selectedNotificationSign

    private val _favoriteSign = MutableStateFlow("Aquarius")
    val favoriteSign: StateFlow<String> = _favoriteSign

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage

    private val _history = MutableStateFlow<List<HoroscopeData>>(emptyList())
    val history: StateFlow<List<HoroscopeData>> = _history

    val availableSigns = HoroscopeRepository.AVAILABLE_SIGNS + HoroscopeRepository.INVALID_SIGN

    val availableNotificationSigns = HoroscopeRepository.AVAILABLE_SIGNS

    val availablePeriods = HoroscopeRepository.AVAILABLE_PERIODS

    private var cacheJob: Job? = null
    private var favoriteApplied = false

    init {
        observeCachedHoroscope()
        observeNetworkRecovery()
        observeSelectedNotificationSign()
        observeFavoriteSign()
        observeManualSync()
        observeHistory()
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

    fun selectFavoriteSign(sign: String) {
        val projectSign = sign.toProjectSign()
        if (projectSign == null) {
            _notificationMessage.value = "No se pudo guardar favorito: signo inválido."
            return
        }

        viewModelScope.launch {
            notificationRepository.saveFavoriteSign(projectSign)
            _favoriteSign.value = projectSign
            _selectedSign.value = projectSign
            _selectedPeriod.value = HoroscopeSyncWorker.DEFAULT_PERIOD
            syncScheduler.enqueuePeriodicSync(projectSign)
            _syncMessage.value = "WorkManager diario configurado para $projectSign."
            fetchHoroscope()
        }
    }

    fun onNotificationPermissionDenied() {
        _notificationMessage.value = "Permiso de notificaciones denegado. Podés habilitarlo desde ajustes del sistema."
    }

    fun refreshWithWorker() {
        _syncMessage.value = "Sincronización en segundo plano programada..."
        syncScheduler.enqueueManualSync(
            sign = _selectedSign.value,
            period = _selectedPeriod.value,
        )
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

    private fun observeFavoriteSign() {
        viewModelScope.launch {
            notificationRepository.favoriteSign.collectLatest { sign ->
                val projectSign = sign?.toProjectSign() ?: "Aquarius"
                _favoriteSign.value = projectSign
                syncScheduler.enqueuePeriodicSync(projectSign)
                if (!favoriteApplied) {
                    favoriteApplied = true
                    _selectedSign.value = projectSign
                    _selectedPeriod.value = HoroscopeSyncWorker.DEFAULT_PERIOD
                    fetchHoroscope()
                }
            }
        }
    }

    private fun observeManualSync() {
        viewModelScope.launch {
            syncScheduler.observeManualSync().collectLatest { workInfo ->
                _syncMessage.value = when (workInfo?.state) {
                    WorkInfo.State.ENQUEUED -> "Sincronización en cola."
                    WorkInfo.State.RUNNING -> "Sincronizando horóscopo en segundo plano..."
                    WorkInfo.State.SUCCEEDED -> {
                        val sign = workInfo.outputData.getString(HoroscopeSyncWorker.KEY_SYNCED_SIGN)
                        val period = workInfo.outputData.getString(HoroscopeSyncWorker.KEY_SYNCED_PERIOD)
                        "Sincronizado: ${sign ?: _selectedSign.value} - ${period ?: _selectedPeriod.value}."
                    }
                    WorkInfo.State.FAILED -> "No se pudo sincronizar en segundo plano."
                    WorkInfo.State.BLOCKED -> "Sincronización esperando otra tarea."
                    WorkInfo.State.CANCELLED -> "Sincronización cancelada."
                    null -> null
                }
            }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            repository.observeHistory().collectLatest { entities ->
                _history.value = entities.map { entity -> entity.toDomain() }
            }
        }
    }

    private fun String.toProjectSign(): String? {
        return HoroscopeRepository.AVAILABLE_SIGNS.firstOrNull { sign ->
            sign.equals(this, ignoreCase = true)
        }
    }
}
