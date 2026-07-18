package com.plataformas.horoscoapp.core.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppSpacing {
    val screen = 16.dp
}

@Composable
fun HeroCard(title: String, subtitle: String) {
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
fun InfoCard(
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
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
fun ZodiacSelectionGrid(items: List<String>, selectedItem: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ZodiacSignCard(sign: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier.height(72.dp).clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = sign.zodiacEmoji(), style = MaterialTheme.typography.headlineSmall)
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
fun HorizontalSelectionList(items: List<String>, selectedItem: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item ->
            FilterChip(
                selected = item == selectedItem,
                onClick = { onSelect(item) },
                label = { Text(text = item.replaceFirstChar { it.uppercase() }, maxLines = 1) },
                modifier = Modifier.height(40.dp),
            )
        }
    }
}

@Composable
fun HoroscopeCard(
    horoscopeText: String,
    sign: String,
    date: String,
    period: String,
    isCached: Boolean,
    updatedAt: Long,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth().heightIn(min = 360.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = sign.zodiacEmoji(), style = MaterialTheme.typography.headlineMedium)
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
                modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp, max = 360.dp).verticalScroll(rememberScrollState()),
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

fun String.zodiacEmoji(): String = when (lowercase(Locale.US)) {
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

fun Long.asReadableTime(): String {
    if (this <= 0L) return "localmente"
    return SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(this))
}
