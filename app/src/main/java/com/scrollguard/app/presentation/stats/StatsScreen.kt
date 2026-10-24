package com.scrollguard.app.presentation.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrollguard.app.core.database.dao.AppBlockStat
import com.scrollguard.app.core.database.dao.DailyBlockStat
import com.scrollguard.app.presentation.common.MetricCard
import com.scrollguard.app.presentation.theme.AccentAmber
import com.scrollguard.app.presentation.theme.AlertRed
import com.scrollguard.app.presentation.theme.CardBorder
import com.scrollguard.app.presentation.theme.DarkBackground
import com.scrollguard.app.presentation.theme.DarkSurface
import com.scrollguard.app.presentation.theme.DarkSurfaceVariant
import com.scrollguard.app.presentation.theme.EmeraldPrimary
import com.scrollguard.app.presentation.theme.FocusCyan
import com.scrollguard.app.presentation.theme.TextMuted
import com.scrollguard.app.presentation.theme.TextPrimary
import com.scrollguard.app.presentation.theme.TextSecondary

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Estatísticas de Foco",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Acompanhe o tempo que você recuperou da armadilha do scroll infinito.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Top Highlights Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Tempo Salvo Total",
                        value = "${uiState.totalTimeSavedMinutes / 60}h ${uiState.totalTimeSavedMinutes % 60}m",
                        subtitle = "de vida recuperada",
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total de Bloqueios",
                        value = uiState.totalBlocks.toString(),
                        subtitle = "tentativas frustradas",
                        icon = Icons.Default.Shield,
                        accentColor = FocusCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Compose Canvas Weekly Bar Chart
            item {
                Text(
                    text = "Intervenções nos Últimos 7 Dias",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            item {
                WeeklyBarChartCard(dailyStats = uiState.weeklyStats)
            }

            // Top Apps Attempting Attention
            item {
                Text(
                    text = "Aplicativos Mais Bloqueados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (uiState.topApps.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sem dados suficientes ainda. Use o smartphone normalmente.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                val maxCount = uiState.topApps.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
                items(uiState.topApps) { appStat ->
                    TopAppProgressItem(appStat = appStat, maxCount = maxCount)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun WeeklyBarChartCard(dailyStats: List<DailyBlockStat>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder.copy(alpha = 0.3f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            val maxCount = (dailyStats.maxOfOrNull { it.count } ?: 5).coerceAtLeast(5)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barCount = 7
                val barWidth = 28.dp.toPx()
                val spaceBetween = (canvasWidth - (barWidth * barCount)) / (barCount + 1)

                // Draw background guidelines
                val linePaintColor = DarkSurfaceVariant.copy(alpha = 0.5f)
                drawLine(
                    color = linePaintColor,
                    start = Offset(0f, canvasHeight * 0.25f),
                    end = Offset(canvasWidth, canvasHeight * 0.25f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = linePaintColor,
                    start = Offset(0f, canvasHeight * 0.75f),
                    end = Offset(canvasWidth, canvasHeight * 0.75f),
                    strokeWidth = 1.dp.toPx()
                )

                // Fill with stats or mock placeholders for last 7 slots
                for (i in 0 until barCount) {
                    val count = if (i < dailyStats.size) dailyStats[i].count else 0
                    val normalizedHeight = (count.toFloat() / maxCount.toFloat()) * (canvasHeight - 20.dp.toPx())
                    val barHeight = normalizedHeight.coerceAtLeast(8.dp.toPx())

                    val left = spaceBetween + i * (barWidth + spaceBetween)
                    val top = canvasHeight - barHeight

                    drawRoundRect(
                        color = if (count > 0) EmeraldPrimary else DarkSurfaceVariant,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun TopAppProgressItem(appStat: AppBlockStat, maxCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder.copy(alpha = 0.2f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appStat.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${appStat.count} bloqueios",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (appStat.count.toFloat() / maxCount.toFloat()).coerceIn(0.05f, 1f)

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = FocusCyan,
                trackColor = DarkSurfaceVariant
            )
        }
    }
}
