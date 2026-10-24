package com.scrollguard.app.service.overlay

enum class OverlayType {
    REELS_INTERVENTION,
    SCROLL_LIMIT_PAUSE,
    ADULT_RESTRICTION
}

data class OverlayData(
    val type: OverlayType,
    val title: String,
    val message: String,
    val details: String,
    val countdownSeconds: Int = 5,
    val onDismiss: () -> Unit = {},
    val onExitToHome: () -> Unit = {}
)
