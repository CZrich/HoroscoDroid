package com.plataformas.horoscoapp.ui.horoscope

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.plataformas.horoscoapp.data.notification.DailyHoroscopeNotifier
import com.plataformas.horoscoapp.data.notification.NotificationConstants.ROUTE_PREFIX
import com.plataformas.horoscoapp.data.repository.NotificationRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plataformas.horoscoapp.ui.state.HoroscopeUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoroscopeApp(
    viewModel: HoroscopeViewModel = viewModel(),
    signFromNotification: String? = null,
    onNotificationSignConsumed: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedSign by viewModel.selectedSign.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val selectedNotificationSign by viewModel.selectedNotificationSign.collectAsState()
    val notificationMessage by viewModel.notificationMessage.collectAsState()
    var pendingNotificationSign by remember { mutableStateOf<String?>(null) }
    var pendingTestNotification by remember { mutableStateOf<TestNotificationType?>(null) }
    var selectedTab by remember { mutableStateOf(AppTab.Horoscope) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val sign = pendingNotificationSign
        pendingNotificationSign = null
        val testType = pendingTestNotification
        pendingTestNotification = null
        if (granted && sign != null) {
            viewModel.selectNotificationSign(sign)
        } else if (granted && testType != null) {
            showTestNotification(
                context = context,
                type = testType,
                sign = selectedNotificationSign ?: selectedSign,
            )
        } else {
            viewModel.onNotificationPermissionDenied()
        }
    }

    LaunchedEffect(signFromNotification) {
        val sign = signFromNotification ?: return@LaunchedEffect
        selectedTab = AppTab.Horoscope
        viewModel.openSignFromNotification(sign)
        onNotificationSignConsumed()
    }

    fun requestNotificationsFor(sign: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            viewModel.selectNotificationSign(sign)
            return
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.selectNotificationSign(sign)
        } else {
            pendingNotificationSign = sign
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun requestTestNotification(type: TestNotificationType) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || hasNotificationPermission(context)) {
            showTestNotification(
                context = context,
                type = type,
                sign = selectedNotificationSign ?: selectedSign,
            )
        } else {
            pendingTestNotification = type
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background,
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Horoscope Studio",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                )
            },
            bottomBar = {
                NavigationBar {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Text(text = tab.icon) },
                            label = { Text(text = tab.label) },
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            ) {
                when (selectedTab) {
                    AppTab.Horoscope -> HoroscopeScreen(
                        uiState = uiState,
                        selectedSign = selectedSign,
                        selectedPeriod = selectedPeriod,
                        availableSigns = viewModel.availableNotificationSigns,
                        availablePeriods = viewModel.availablePeriods,
                        onSignSelected = viewModel::setSign,
                        onPeriodSelected = viewModel::setPeriod,
                        onRetry = viewModel::fetchHoroscope,
                    )
                    AppTab.Settings -> SettingsScreen(
                        selectedNotificationSign = selectedNotificationSign,
                        notificationMessage = notificationMessage,
                        availableSigns = viewModel.availableNotificationSigns,
                        onSignSelected = ::requestNotificationsFor,
                    )
                    AppTab.Tests -> NotificationTestScreen(
                        selectedSign = selectedNotificationSign ?: selectedSign,
                        onDataMessageClick = { requestTestNotification(TestNotificationType.Data) },
                        onNotificationMessageClick = { requestTestNotification(TestNotificationType.Notification) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HoroscopeScreen(
    uiState: HoroscopeUiState,
    selectedSign: String,
    selectedPeriod: String,
    availableSigns: List<String>,
    availablePeriods: List<String>,
    onSignSelected: (String) -> Unit,
    onPeriodSelected: (String) -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            HeroCard(
                title = "Tu lectura astral",
                subtitle = "Elegí un signo y consultá el horóscopo actualizado desde la API actual.",
            )
        }
        item {
            SectionLabel(text = "Signo zodiacal")
            Spacer(modifier = Modifier.height(8.dp))
            ZodiacSelectionGrid(
                items = availableSigns,
                selectedItem = selectedSign,
                onSelect = onSignSelected,
            )
        }
        item {
            SectionLabel(text = "Periodo")
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalSelectionList(
                items = availablePeriods,
                selectedItem = selectedPeriod,
                onSelect = onPeriodSelected,
            )
        }
        item {
            when (uiState) {
                is HoroscopeUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is HoroscopeUiState.Success -> {
                    val horoscope = uiState.horoscope
                    HoroscopeCard(
                        horoscopeText = horoscope.horoscope,
                        sign = horoscope.sign,
                        date = horoscope.date,
                        period = horoscope.period,
                        isCached = uiState.isCached,
                        updatedAt = uiState.updatedAt,
                    )
                }
                is HoroscopeUiState.Error -> {
                    ErrorBox(
                        message = uiState.message,
                        onRetry = onRetry,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    selectedNotificationSign: String?,
    notificationMessage: String?,
    availableSigns: List<String>,
    onSignSelected: (String) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            HeroCard(
                title = "Notificaciones diarias",
                subtitle = "Seleccioná el signo del topic FCM. La notificación solo avisa; el horóscopo se consulta al abrir la app.",
            )
        }
        item {
            SettingsCard(
                title = "Signo para push",
                body = "Cuando cambies el signo, la app se desuscribe del topic anterior y se suscribe al nuevo.",
            ) {
                ZodiacSelectionGrid(
                    items = availableSigns,
                    selectedItem = selectedNotificationSign.orEmpty(),
                    onSelect = onSignSelected,
                )
            }
        }
        item {
            SettingsCard(
                title = "Topic actual",
                body = selectedNotificationSign?.let { NotificationRepository.topicForSign(it) }
                    ?: "Todavía no seleccionaste un signo para notificaciones.",
            )
        }
        notificationMessage?.let { message ->
            item {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun NotificationTestScreen(
    selectedSign: String,
    onDataMessageClick: () -> Unit,
    onNotificationMessageClick: () -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            HeroCard(
                title = "Pruebas para el informe",
                subtitle = "Estos botones muestran notificaciones reales de Android usando el signo $selectedSign.",
            )
        }
        item {
            SettingsCard(
                title = "Data Message",
                body = "Replica el flujo de un FCM data message: type, sign, title, message y route. Al tocarla abre el horóscopo del signo.",
            ) {
                Button(
                    onClick = onDataMessageClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Enviar prueba Data Message")
                }
            }
        }
        item {
            SettingsCard(
                title = "Notification Message",
                body = "Replica el formato de Firebase Console con título y mensaje, pero generado localmente para poder probarlo ahora.",
            ) {
                Button(
                    onClick = onNotificationMessageClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Enviar prueba Notification Message")
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    title: String,
    subtitle: String,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    body: String,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun ZodiacSelectionGrid(
    items: List<String>,
    selectedItem: String,
    onSelect: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowItems.forEach { item ->
                    ZodiacSignCard(
                        sign = item,
                        selected = item == selectedItem,
                        onClick = { onSelect(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ZodiacSignCard(
    sign: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = sign.zodiacEmoji(),
                style = MaterialTheme.typography.headlineSmall,
            )
            Column {
                Text(
                    text = sign,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (selected) "Seleccionado" else "Tocar para elegir",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                )
            }
        }
    }
}

@Composable
private fun HorizontalSelectionList(
    items: List<String>,
    selectedItem: String,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items) { item ->
            FilterChip(
                selected = item == selectedItem,
                onClick = { onSelect(item) },
                label = {
                    Text(
                        text = item.replaceFirstChar { it.uppercase() },
                        maxLines = 1,
                    )
                },
                modifier = Modifier.height(40.dp)
            )
        }
    }
}

@Composable
private fun HoroscopeCard(
    horoscopeText: String,
    sign: String,
    date: String,
    period: String,
    isCached: Boolean,
    updatedAt: Long,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 360.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sign.zodiacEmoji(),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Column {
                    Text(
                        text = "${sign.uppercase()} - ${period.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = horoscopeText.ifBlank { "No hay texto de horóscopo disponible para este signo y periodo." },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = if (isCached) "Offline - guardado ${updatedAt.asReadableTime()}" else "Actualizado ${updatedAt.asReadableTime()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

private fun Long.asReadableTime(): String {
    if (this <= 0L) return "localmente"
    val formatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    return formatter.format(Date(this))
}

private fun hasNotificationPermission(context: android.content.Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
}

private fun showTestNotification(
    context: android.content.Context,
    type: TestNotificationType,
    sign: String,
) {
    val route = "$ROUTE_PREFIX${sign.lowercase(Locale.US)}"
    val message = "Descubre lo que los astros tienen preparado hoy para $sign."
    val notifier = DailyHoroscopeNotifier(context)

    when (type) {
        TestNotificationType.Data -> notifier.showDailyHoroscopeNotification(
            title = "Tu horóscopo diario de $sign",
            message = message,
            sign = sign,
            route = route,
        )
        TestNotificationType.Notification -> notifier.showDailyHoroscopeNotification(
            title = "Tu horóscopo de hoy",
            message = "Revisa lo que los astros tienen preparado para ti.",
            sign = sign,
            route = route,
        )
    }
}

private fun String.zodiacEmoji(): String {
    return when (lowercase(Locale.US)) {
        "aries" -> "♈"
        "taurus" -> "♉"
        "gemini" -> "♊"
        "cancer" -> "♋"
        "leo" -> "♌"
        "virgo" -> "♍"
        "libra" -> "♎"
        "scorpio" -> "♏"
        "sagittarius" -> "♐"
        "capricorn" -> "♑"
        "aquarius" -> "♒"
        "pisces" -> "♓"
        else -> "✦"
    }
}

private enum class AppTab(
    val label: String,
    val icon: String,
) {
    Horoscope("Horóscopo", "♈"),
    Settings("Settings", "⚙"),
    Tests("Pruebas", "✉"),
}

private enum class TestNotificationType {
    Data,
    Notification,
}

@Composable
private fun ErrorBox(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Algo salió mal",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRetry) {
                Text(text = "Reintentar")
            }
        }
    }
}
