package com.scrollguard.app.presentation.dashboard

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoAdultContent
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrollguard.app.domain.model.BlockLog
import com.scrollguard.app.domain.model.BlockType
import com.scrollguard.app.presentation.common.GuardianStatusCard
import com.scrollguard.app.presentation.common.MetricCard
import com.scrollguard.app.presentation.common.ModernSwitchCard
import com.scrollguard.app.presentation.theme.AccentAmber
import com.scrollguard.app.presentation.theme.AlertRed
import com.scrollguard.app.presentation.theme.CardBorder
import com.scrollguard.app.presentation.theme.DarkBackground
import com.scrollguard.app.presentation.theme.DarkSurface
import com.scrollguard.app.presentation.theme.DarkSurfaceVariant
import com.scrollguard.app.presentation.theme.EmeraldPrimary
import com.scrollguard.app.presentation.theme.FocusCyan
import com.scrollguard.app.presentation.theme.FocusIndigo
import com.scrollguard.app.presentation.theme.TextMuted
import com.scrollguard.app.presentation.theme.TextPrimary
import com.scrollguard.app.presentation.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToOemGuide: () -> Unit,
    onNavigateToStrictMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkStatus()
    }

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
                // App Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ScrollGuard",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Defesa Anti-Dopamina Ativa",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToOemGuide,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Guia Anti-Kill",
                            tint = TextPrimary
                        )
                    }
                }
            }

            // Status Card
            item {
                GuardianStatusCard(
                    isActive = uiState.guardianStatus.isProtectionActive,
                    isServiceRunning = uiState.guardianStatus.isServiceEnabled,
                    onFixClicked = onNavigateToOnboarding
                )
            }

            // Daily Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Bloqueios",
                        value = uiState.stats.todayBlocksCount.toString(),
                        subtitle = "distrações hoje",
                        icon = Icons.Default.Block,
                        accentColor = AlertRed,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Tempo Salvo",
                        value = "${uiState.stats.todayTimeSavedMinutes}m",
                        subtitle = "preservados",
                        icon = Icons.Default.Timer,
                        accentColor = FocusCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Ofensiva",
                        value = "${uiState.stats.streakDays}d",
                        subtitle = "foco contínuo",
                        icon = Icons.Default.Bolt,
                        accentColor = AccentAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Protection Controls Section
            item {
                Text(
                    text = "Módulos de Proteção",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            item {
                ModernSwitchCard(
                    title = "Bloquear Reels & Shorts",
                    description = "Intercepta vídeos verticais no Instagram, YouTube, TikTok e Facebook.",
                    checked = uiState.preferences.isAntiReelsActive,
                    onCheckedChange = { viewModel.toggleAntiReels(it) },
                    icon = Icons.Default.Videocam
                )
            }

            item {
                ModernSwitchCard(
                    title = "Limite de Rolagem (Anti-Scroll)",
                    description = "Pausa o feed infinito após ${uiState.preferences.scrollSessionLimitMinutes} min com timer reflexivo.",
                    checked = uiState.preferences.isAntiScrollActive,
                    onCheckedChange = { viewModel.toggleAntiScroll(it) },
                    icon = Icons.Default.SwapVert
                )
            }

            item {
                ModernSwitchCard(
                    title = "Bloqueador de Conteúdo Adulto",
                    description = "Filtro em navegadores para domínios impróprios e termos restritos.",
                    checked = uiState.preferences.isAdultFilterActive,
                    onCheckedChange = { viewModel.toggleAdultFilter(it) },
                    icon = Icons.Default.NoAdultContent
                )
            }

            item {
                Card(
                    onClick = onNavigateToStrictMode,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FocusIndigo.copy(alpha = 0.4f)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FocusIndigo.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = FocusIndigo,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Modo Estrito (Anti-Impulso)",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (uiState.preferences.isStrictModeActive)
                                    "Ativo! Desativações exigem desafio de digitação/espera."
                                else
                                    "Desative a tentação de desligar a proteção por impulso.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Recent Logs Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Histórico Recente de Intervenções",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            if (uiState.recentLogs.isEmpty()) {
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
                                text = "Nenhuma distração detectada ainda. Continue focado!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(uiState.recentLogs) { log ->
                    RecentLogItem(log)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Strict Mode Challenge Dialog if user tries to disable switches impulsively
        if (uiState.showStrictChallengeDialog) {
            StrictModeChallengeDialog(
                challengeType = uiState.preferences.strictModeChallengeType,
                onSuccess = { viewModel.onChallengeCompleted() },
                onDismiss = { viewModel.dismissChallengeDialog() }
            )
        }
    }
}

@Composable
fun RecentLogItem(log: BlockLog) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = remember(log.timestamp) { timeFormatter.format(Date(log.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder.copy(alpha = 0.2f)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, color) = when (log.blockType) {
                BlockType.REELS -> Icons.Default.Videocam to AlertRed
                BlockType.SCROLL_LIMIT -> Icons.Default.HourglassBottom to FocusCyan
                BlockType.ADULT_CONTENT -> Icons.Default.NoAdultContent to AlertRed
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StrictModeChallengeDialog(
    challengeType: String,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val targetPhrase = "Eu decido ter foco na minha vida real"
    var typedText by remember { mutableStateOf("") }
    var secondsRemaining by remember { mutableIntStateOf(10) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Desafio do Modo Estrito",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Para evitar desativações impulsivas geradas pelo vício em dopamina, complete o desafio consciente:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Digite a frase com exatidão:",
                    style = MaterialTheme.typography.labelSmall,
                    color = FocusIndigo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "\"$targetPhrase\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Digite a frase acima...") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (secondsRemaining > 0) {
                    Text(
                        text = "Aguarde o tempo consciente: ${secondsRemaining}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = AlertRed
                    )
                } else {
                    Text(
                        text = "Tempo consciente concluído.",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldPrimary
                    )
                }
            }
        },
        confirmButton = {
            val isPhraseMatch = typedText.trim().equals(targetPhrase, ignoreCase = false)
            val canConfirm = isPhraseMatch && secondsRemaining <= 0

            Button(
                onClick = onSuccess,
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Confirmar Mudança", color = DarkBackground)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Desistir e Manter Foco", color = TextPrimary)
            }
        }
    )
}
