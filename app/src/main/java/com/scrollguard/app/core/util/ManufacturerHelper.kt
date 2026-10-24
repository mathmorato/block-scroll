package com.scrollguard.app.core.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.util.Locale

enum class OemBrand {
    XIAOMI,
    SAMSUNG,
    HUAWEI,
    OPPO,
    VIVO,
    ONEPLUS,
    OTHER
}

data class OemInstruction(
    val title: String,
    val description: String,
    val videoAssetPath: String? = null
)

object ManufacturerHelper {

    fun getDeviceBrand(): OemBrand {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.ROOT)
        return when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> OemBrand.XIAOMI
            manufacturer.contains("samsung") -> OemBrand.SAMSUNG
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> OemBrand.HUAWEI
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> OemBrand.OPPO
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> OemBrand.VIVO
            manufacturer.contains("oneplus") -> OemBrand.ONEPLUS
            else -> OemBrand.OTHER
        }
    }

    fun getBrandName(): String {
        return Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    fun getOemInstructions(brand: OemBrand): List<OemInstruction> {
        return when (brand) {
            OemBrand.XIAOMI -> listOf(
                OemInstruction(
                    title = "1. Ativar Início Automático (Autostart)",
                    description = "O MIUI / HyperOS desativa serviços em segundo plano. Abra o app Segurança > Permissões > Início Automático e habilite o ScrollGuard.",
                    videoAssetPath = "tutorials/xiaomi_autostart.mp4"
                ),
                OemInstruction(
                    title = "2. Bloquear no Gerenciador de Recentes (Cadeado)",
                    description = "Abra os aplicativos recentes, segure o card do ScrollGuard ou puxe para baixo e toque no ícone de Cadeado para impedir que a limpeza de memória encerre o serviço.",
                    videoAssetPath = "tutorials/xiaomi_lock.mp4"
                ),
                OemInstruction(
                    title = "3. Economia de Bateria: Sem Restrições",
                    description = "Nas configurações do app ScrollGuard > Economia de Bateria, selecione 'Nenhuma restrição' para evitar que o serviço de acessibilidade seja desativado.",
                    videoAssetPath = "tutorials/xiaomi_battery.mp4"
                )
            )
            OemBrand.SAMSUNG -> listOf(
                OemInstruction(
                    title = "1. Desativar Suspensão Automática (App Nunca Suspenso)",
                    description = "Acesse Configurações > Assistência do Aparelho > Bateria > Limites de uso em segundo plano > Apps nunca suspensos e adicione o ScrollGuard.",
                    videoAssetPath = "tutorials/samsung_never_sleep.mp4"
                ),
                OemInstruction(
                    title = "2. Bloquear em Aplicativos Recentes",
                    description = "Abra a tela de apps recentes, toque no ícone do ScrollGuard e escolha 'Bloquear este aplicativo'.",
                    videoAssetPath = "tutorials/samsung_lock.mp4"
                )
            )
            OemBrand.HUAWEI -> listOf(
                OemInstruction(
                    title = "1. Gerenciamento Manual de Inicialização",
                    description = "Configurações > Bateria > Inicialização do Aplicativo. Desmarque ScrollGuard e selecione 'Gerenciar manualmente' (Início automático, inicialização secundária e execução em segundo plano).",
                    videoAssetPath = "tutorials/huawei_launch.mp4"
                ),
                OemInstruction(
                    title = "2. Ignorar Otimização de Bateria",
                    description = "Configurações > Aplicativos > Acesso Especial > Ignorar Otimização de Bateria > Permitir para ScrollGuard.",
                    videoAssetPath = "tutorials/huawei_battery.mp4"
                )
            )
            else -> listOf(
                OemInstruction(
                    title = "1. Ignorar Otimização de Bateria",
                    description = "Permita que o ScrollGuard execute sem limitações de segundo plano para garantir proteção contínua.",
                    videoAssetPath = null
                ),
                OemInstruction(
                    title = "2. Manter na Memória",
                    description = "Fixe o aplicativo na tela de recentes se o seu dispositivo fechar aplicativos em segundo plano.",
                    videoAssetPath = null
                )
            )
        }
    }

    fun openAutostartSettings(context: Context): Boolean {
        val intents = listOf(
            // Xiaomi / MIUI / HyperOS
            Intent().apply {
                component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            },
            // Huawei
            Intent().apply {
                component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
            },
            // Samsung Device Care
            Intent().apply {
                component = ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
            },
            // Oppo
            Intent().apply {
                component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            },
            // Vivo
            Intent().apply {
                component = ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
            }
        )

        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return true
            } catch (_: Exception) {
                // Try next
            }
        }

        // Fallback: app details settings
        return try {
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
            true
        } catch (_: Exception) {
            false
        }
    }
}
