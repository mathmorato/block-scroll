package com.scrollguard.app.presentation.onboarding

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.scrollguard.app.core.util.OemBrand
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
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToOemGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Recheck permissions whenever user returns to the app from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Configure seu Guardião",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "O ScrollGuard precisa de 2 permissões fundamentais para detectar e bloquear o fluxo infinito de vídeos e feeds.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }

            // Step 1: Accessibility Service
            item {
                PermissionStepCard(
                    title = "1. Serviço de Acessibilidade",
                    description = "Necessário para identificar telas de Reels, Shorts e contabilizar o tempo de rolagem.",
                    isGranted = uiState.isAccessibilityGranted,
                    icon = Icons.Default.Security,
                    buttonLabel = "Ativar no Sistema",
                    onAction = { viewModel.requestAccessibility() }
                )
            }

            // Step 2: Overlay Permission
            item {
                PermissionStepCard(
                    title = "2. Exibir Sobre Outros Apps",
                    description = "Permite mostrar o alerta consciente em tela cheia com o timer de pausa reflexiva.",
                    isGranted = uiState.isOverlayGranted,
                    icon = Icons.Default.Layers,
                    buttonLabel = "Permitir Sobreposição",
                    onAction = { viewModel.requestOverlay() }
                )
            }

            // Step 3: Battery Optimization
            item {
                PermissionStepCard(
                    title = "3. Ignorar Otimização de Bateria",
                    description = "Impede que o Android mate o guardião em segundo plano quando a tela estiver apagada.",
                    isGranted = uiState.isBatteryIgnored,
                    icon = Icons.Default.BatteryAlert,
                    buttonLabel = "Desativar Economia",
                    onAction = { viewModel.requestIgnoreBattery() }
                )
            }

            // OEM Guide Banner
            if (uiState.oemBrand != OemBrand.OTHER) {
                item {
                    Card(
                        onClick = onNavigateToOemGuide,
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(FocusCyan.copy(alpha = 0.4f))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dispositivo ${uiState.brandName} Detectado!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FocusCyan
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Toque aqui para ver o guia anti-kill (Início Automático e Cadeado nos Recentes).",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = FocusCyan
                            )
                        }
                    }
                }
            }

            // Finish button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.completeOnboarding()
                        onNavigateToDashboard()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = uiState.areCorePermissionsGranted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        disabledContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (uiState.areCorePermissionsGranted) "Tudo Pronto! Entrar no ScrollGuard" else "Habilite as Permissões Obrigatórias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.areCorePermissionsGranted) DarkBackground else TextMuted
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PermissionStepCard(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    buttonLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isGranted) EmeraldPrimary.copy(alpha = 0.5f) else CardBorder.copy(alpha = 0.3f)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) EmeraldPrimary.copy(alpha = 0.15f) else DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.Check else icon,
                        contentDescription = null,
                        tint = if (isGranted) EmeraldPrimary else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isGranted) "Concedida com sucesso" else "Pendente",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isGranted) EmeraldPrimary else AlertRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary)
                ) {
                    Text(
                        text = buttonLabel,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
