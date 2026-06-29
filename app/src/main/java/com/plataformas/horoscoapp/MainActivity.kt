package com.plataformas.horoscoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.plataformas.horoscoapp.ui.horoscope.HoroscopeApp
import com.plataformas.horoscoapp.ui.theme.HoroscoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HoroscoAppTheme {
                HoroscopeApp()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HoroscopePreview() {
    HoroscoAppTheme {
        HoroscopeApp()
    }
}