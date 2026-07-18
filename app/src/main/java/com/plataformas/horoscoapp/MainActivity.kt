package com.plataformas.horoscoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.plataformas.horoscoapp.data.notification.NotificationConstants.EXTRA_ROUTE
import com.plataformas.horoscoapp.data.notification.NotificationConstants.EXTRA_SIGN
import com.plataformas.horoscoapp.app.HoroscopeApp
import com.plataformas.horoscoapp.ui.theme.HoroscoAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val notificationSign = MutableStateFlow<String?>(null)
    private var lastNotificationRoute: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntent(intent)
        setContent {
            val signFromNotification by notificationSign.collectAsState()

            HoroscoAppTheme {
                HoroscopeApp(
                    signFromNotification = signFromNotification,
                    onNotificationSignConsumed = { notificationSign.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val sign = intent?.getStringExtra(EXTRA_SIGN)?.takeIf { it.isNotBlank() }
            ?: intent?.getStringExtra("sign")?.takeIf { it.isNotBlank() }
            ?: intent?.getStringExtra(EXTRA_ROUTE)?.signFromRoute()
            ?: intent?.getStringExtra("route")?.signFromRoute()
            ?: return
        val route = intent?.getStringExtra(EXTRA_ROUTE)
            ?: intent?.getStringExtra("route")
            ?: sign
        if (route == lastNotificationRoute) return

        lastNotificationRoute = route
        notificationSign.value = sign
    }

    private fun String.signFromRoute(): String? {
        return substringAfter("horoscope/", missingDelimiterValue = "")
            .takeIf { it.isNotBlank() }
    }
}


@Preview(showBackground = true)
@Composable
fun HoroscopePreview() {
    HoroscoAppTheme {
        HoroscopeApp()
    }
}
