package com.scrollguard.app.service.accessibility.detector

import android.view.accessibility.AccessibilityNodeInfo

class YouTubeDetector : ContentDetector {
    override val supportedPackage: String = "com.google.android.youtube"

    private val shortsViewIds = listOf(
        "com.google.android.youtube:id/shorts_recycler_view",
        "com.google.android.youtube:id/shorts_player_fragment",
        "com.google.android.youtube:id/reel_player_fragment_container",
        "com.google.android.youtube:id/reel_recycler",
        "com.google.android.youtube:id/shorts_shelf_container"
    )

    override fun detect(rootNode: AccessibilityNodeInfo?): DetectionResult {
        if (rootNode == null) return DetectionResult(false)

        // 1. Check known YouTube Shorts container View IDs
        for (viewId in shortsViewIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            if (nodes.isNotEmpty()) {
                nodes.forEach { it.recycle() }
                return DetectionResult(
                    isDistractionDetected = true,
                    reason = "YouTube Shorts Detectado",
                    details = "Player vertical de Shorts em reprodução"
                )
            }
        }

        // 2. Check if the bottom pivot bar has "Shorts" selected
        val shortsTabs = rootNode.findAccessibilityNodeInfosByText("Shorts")
        for (tab in shortsTabs) {
            if (tab.isSelected || tab.parent?.isSelected == true) {
                tab.recycle()
                return DetectionResult(
                    isDistractionDetected = true,
                    reason = "Aba Shorts Ativa no YouTube",
                    details = "Navegação na aba dedicada de Shorts"
                )
            }
            tab.recycle()
        }

        return DetectionResult(false)
    }
}
