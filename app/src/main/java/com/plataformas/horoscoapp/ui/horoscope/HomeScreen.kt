package com.plataformas.horoscoapp.ui.horoscope

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plataformas.horoscoapp.ui.state.HoroscopeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoroscopeApp(
    viewModel: HoroscopeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedSign by viewModel.selectedSign.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

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
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Elige tu signo y periodo para ver tu horóscopo",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                SectionLabel(text = "Signo")
                HorizontalSelectionList(
                    items = viewModel.availableSigns,
                    selectedItem = selectedSign,
                    onSelect = viewModel::setSign,
                )

                SectionLabel(text = "Periodo")
                HorizontalSelectionList(
                    items = viewModel.availablePeriods,
                    selectedItem = selectedPeriod,
                    onSelect = viewModel::setPeriod,
                )

                when (uiState) {
                    is HoroscopeUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is HoroscopeUiState.Success -> {
                        val horoscope = (uiState as HoroscopeUiState.Success).horoscope
                        HoroscopeCard(
                            horoscopeText = horoscope.horoscope,
                            sign = horoscope.sign,
                            date = horoscope.date,
                            period = horoscope.period,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    is HoroscopeUiState.Error -> {
                        val message = (uiState as HoroscopeUiState.Error).message
                        ErrorBox(
                            message = message,
                            onRetry = viewModel::fetchHoroscope,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${sign.uppercase()} - ${period.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = horoscopeText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            )
            TextButton(onClick = {}) {
                Text(text = "Actualizado al vuelo")
            }
        }
    }
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
