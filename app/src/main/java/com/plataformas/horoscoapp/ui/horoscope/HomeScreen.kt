package com.plataformas.horoscoapp.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.plataformas.horoscoapp.BuildConfig
import com.plataformas.horoscoapp.core.designsystem.AppSpacing
import com.plataformas.horoscoapp.feature.horoscope.HistoryScreen
import com.plataformas.horoscoapp.feature.horoscope.HoroscopeScreen
import com.plataformas.horoscoapp.feature.horoscope.HoroscopeViewModel
import com.plataformas.horoscoapp.feature.horoscope.SettingsScreen
import com.plataformas.horoscoapp.feature.horoscope.NotificationTestScreen
import com.plataformas.horoscoapp.feature.tests.showTestNotification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoroscopeApp(
    viewModel: HoroscopeViewModel = hiltViewModel(),
    signFromNotification: String? = null,
    onNotificationSignConsumed: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedSign by viewModel.selectedSign.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val selectedNotificationSign by viewModel.selectedNotificationSign.collectAsStateWithLifecycle()
    val favoriteSign by viewModel.favoriteSign.collectAsStateWithLifecycle()
    val notificationMessage by viewModel.notificationMessage.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    var pendingNotificationSign by remember { mutableStateOf<String?>(null) }
    var pendingTestNotification by remember { mutableStateOf<TestNotificationType?>(null) }
    val navController = rememberNavController()
    val visibleTabs = remember { AppTab.entries.filter { tab -> !tab.debugOnly || BuildConfig.DEBUG } }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppTab.Horoscope.route
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val sign = pendingNotificationSign
        val testType = pendingTestNotification
        pendingNotificationSign = null
        pendingTestNotification = null

        when {
            granted && sign != null -> viewModel.selectNotificationSign(sign)
            granted && testType != null -> showTestNotification(
                context = context,
                type = testType,
                sign = selectedNotificationSign ?: selectedSign,
            )
            else -> viewModel.onNotificationPermissionDenied()
        }
    }

    LaunchedEffect(signFromNotification) {
        val sign = signFromNotification ?: return@LaunchedEffect
        navController.navigate(AppTab.Horoscope.route) {
            launchSingleTop = true
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            restoreState = true
        }
        viewModel.openSignFromNotification(sign)
        onNotificationSignConsumed()
    }

    fun requestNotificationsFor(sign: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || hasNotificationPermission(context)) {
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
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background,
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "AstroDaily") },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
                )
            },
            bottomBar = {
                NavigationBar {
                    visibleTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    launchSingleTop = true
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    restoreState = true
                                }
                            },
                            icon = { Text(text = tab.icon) },
                            label = { Text(text = tab.label) },
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(AppSpacing.screen),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = AppTab.Horoscope.route,
                ) {
                    composable(AppTab.Horoscope.route) {
                        HoroscopeScreen(
                            uiState = uiState,
                            selectedSign = selectedSign,
                            selectedPeriod = selectedPeriod,
                            availableSigns = viewModel.availableNotificationSigns,
                            availablePeriods = viewModel.availablePeriods,
                            onSignSelected = viewModel::setSign,
                            onPeriodSelected = viewModel::setPeriod,
                            onRetry = viewModel::fetchHoroscope,
                            syncMessage = syncMessage,
                            onBackgroundRefresh = viewModel::refreshWithWorker,
                        )
                    }
                    composable(AppTab.Settings.route) {
                        SettingsScreen(
                            favoriteSign = favoriteSign,
                            selectedNotificationSign = selectedNotificationSign,
                            notificationMessage = notificationMessage,
                            availableSigns = viewModel.availableNotificationSigns,
                            onFavoriteSignSelected = viewModel::selectFavoriteSign,
                            onSignSelected = ::requestNotificationsFor,
                        )
                    }
                    composable(AppTab.History.route) {
                        HistoryScreen(history = history)
                    }
                    composable(AppTab.Tests.route) {
                        NotificationTestScreen(
                            selectedSign = selectedNotificationSign ?: selectedSign,
                            onDataMessageClick = { requestTestNotification(TestNotificationType.Data) },
                            onNotificationMessageClick = { requestTestNotification(TestNotificationType.Notification) },
                        )
                    }
                }
            }
        }
    }
}

private fun hasNotificationPermission(context: android.content.Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
}
