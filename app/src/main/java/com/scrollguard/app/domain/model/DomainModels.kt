package com.scrollguard.app.domain.model

enum class BlockType(val displayName: String) {
    REELS("Vídeo Curto (Reels/Shorts)"),
    SCROLL_LIMIT("Limite de Rolagem"),
    ADULT_CONTENT("Domínio Adulto")
}

data class BlockLog(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val blockType: BlockType,
    val details: String,
    val timestamp: Long,
    val timeSavedSeconds: Long
)

data class AppRule(
    val packageName: String,
    val appName: String,
    val isMonitored: Boolean = true,
    val isReelsBlocked: Boolean = true,
    val isScrollLimitBlocked: Boolean = true,
    val customScrollLimitMinutes: Int = 10,
    val iconUri: String? = null
)

data class GuardianStatus(
    val isServiceEnabled: Boolean,
    val isOverlayPermissionGranted: Boolean,
    val isBatteryOptimizationIgnored: Boolean,
    val isProtectionActive: Boolean
) {
    val isFullyFunctional: Boolean
        get() = isServiceEnabled && isOverlayPermissionGranted && isProtectionActive
}

data class StatsSummary(
    val todayBlocksCount: Int = 0,
    val todayTimeSavedMinutes: Long = 0,
    val totalBlocksCount: Int = 0,
    val totalTimeSavedMinutes: Long = 0,
    val streakDays: Int = 1,
    val topApps: List<Pair<String, Int>> = emptyList(),
    val weeklyStats: List<Pair<String, Int>> = emptyList()
)
