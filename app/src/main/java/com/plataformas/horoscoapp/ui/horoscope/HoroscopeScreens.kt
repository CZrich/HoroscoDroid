package com.plataformas.horoscoapp.feature.horoscope

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.plataformas.horoscoapp.data.model.HoroscopeData
import com.plataformas.horoscoapp.data.repository.NotificationRepository
import com.plataformas.horoscoapp.core.designsystem.HeroCard
import com.plataformas.horoscoapp.core.designsystem.HoroscopeCard
import com.plataformas.horoscoapp.core.designsystem.HorizontalSelectionList
import com.plataformas.horoscoapp.core.designsystem.InfoCard
import com.plataformas.horoscoapp.core.designsystem.SectionLabel
import com.plataformas.horoscoapp.core.designsystem.ZodiacSelectionGrid
import com.plataformas.horoscoapp.core.designsystem.zodiacEmoji
import com.plataformas.horoscoapp.ui.state.HoroscopeUiState

@Composable
fun HoroscopeScreen(
    uiState: HoroscopeUiState,
    selectedSign: String,
    selectedPeriod: String,
    availableSigns: List<String>,
    availablePeriods: List<String>,
    onSignSelected: (String) -> Unit,
    onPeriodSelected: (String) -> Unit,
    onRetry: () -> Unit,
    syncMessage: String?,
    onBackgroundRefresh: () -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
        item { HeroCard("Tu lectura astral", "Elegí un signo y consultá el horóscopo actualizado desde la API actual.") }
        item {
            SectionLabel("Signo zodiacal")
            Spacer(Modifier.height(8.dp))
            ZodiacSelectionGrid(availableSigns, selectedSign, onSignSelected)
        }
        item {
            SectionLabel("Periodo")
            Spacer(Modifier.height(8.dp))
            HorizontalSelectionList(availablePeriods, selectedPeriod, onPeriodSelected)
        }
        item {
            InfoCard(
                title = "Sincronización con WorkManager",
                body = syncMessage ?: "Actualizá el horóscopo en segundo plano. La tarea usa restricciones de red, retry y outputData.",
            ) {
                Button(onClick = onBackgroundRefresh, modifier = Modifier.fillMaxWidth()) {
                    Text("Actualizar en segundo plano")
                }
            }
        }
        item {
            when (uiState) {
                is HoroscopeUiState.Loading -> Box(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
                is HoroscopeUiState.Success -> HoroscopeCard(
                    horoscopeText = uiState.horoscope.horoscope,
                    sign = uiState.horoscope.sign,
                    date = uiState.horoscope.date,
                    period = uiState.horoscope.period,
                    isCached = uiState.isCached,
                    updatedAt = uiState.updatedAt,
                )
                is HoroscopeUiState.Error -> ErrorBox(uiState.message, onRetry)
            }
        }
    }
}

@Composable
fun SettingsScreen(
    favoriteSign: String,
    selectedNotificationSign: String?,
    notificationMessage: String?,
    availableSigns: List<String>,
    onFavoriteSignSelected: (String) -> Unit,
    onSignSelected: (String) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
        item { HeroCard("Ajustes astrales", "Configurá tu signo favorito, sincronización diaria y notificaciones push por topic FCM.") }
        item {
            InfoCard("Signo favorito", "Este signo se abre por defecto y alimenta la sincronización periódica de WorkManager.") {
                ZodiacSelectionGrid(availableSigns, favoriteSign, onFavoriteSignSelected)
            }
        }
        item {
            InfoCard("Signo para push", "Cuando cambies el signo, la app se desuscribe del topic anterior y se suscribe al nuevo.") {
                ZodiacSelectionGrid(availableSigns, selectedNotificationSign.orEmpty(), onSignSelected)
            }
        }
        item {
            InfoCard(
                title = "Topic actual",
                body = selectedNotificationSign?.let { NotificationRepository.topicForSign(it) }
                    ?: "Todavía no seleccionaste un signo para notificaciones.",
            )
        }
        notificationMessage?.let { message ->
            item { Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
fun HistoryScreen(history: List<HoroscopeData>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        item { HeroCard("Historial offline", "Lecturas guardadas localmente en Room. Podés revisarlas aunque no tengas conexión.") }
        if (history.isEmpty()) {
            item { InfoCard("Sin lecturas guardadas", "Consultá un horóscopo o ejecutá una sincronización para crear historial local.") }
        } else {
            items(history) { horoscope -> HistoryItem(horoscope) }
        }
    }
}

@Composable
fun NotificationTestScreen(
    selectedSign: String,
    onDataMessageClick: () -> Unit,
    onNotificationMessageClick: () -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
        item { HeroCard("Pruebas para el informe", "Estos botones muestran notificaciones reales de Android usando el signo $selectedSign.") }
        item {
            InfoCard("Data Message", "Replica el flujo de un FCM data message: type, sign, title, message y route. Al tocarla abre el horóscopo del signo.") {
                Button(onClick = onDataMessageClick, modifier = Modifier.fillMaxWidth()) { Text("Enviar prueba Data Message") }
            }
        }
        item {
            InfoCard("Notification Message", "Replica el formato de Firebase Console con título y mensaje, pero generado localmente para poder probarlo ahora.") {
                Button(onClick = onNotificationMessageClick, modifier = Modifier.fillMaxWidth()) { Text("Enviar prueba Notification Message") }
            }
        }
    }
}

@Composable
private fun HistoryItem(horoscope: HoroscopeData) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "${horoscope.sign.zodiacEmoji()} ${horoscope.sign} - ${horoscope.period.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(horoscope.date, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = horoscope.horoscope.ifBlank { "Sin contenido guardado." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
            )
        }
    }
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Algo salió mal", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
            Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}
