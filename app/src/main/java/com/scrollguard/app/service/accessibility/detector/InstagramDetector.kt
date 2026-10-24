package com.scrollguard.app.service.accessibility.detector

import android.view.accessibility.AccessibilityNodeInfo

class InstagramDetector : ContentDetector {
    override val supportedPackage: String = "com.instagram.android"

    private val reelsViewIds = listOf(
        "com.instagram.android:id/reel_viewer_root",
        "com.instagram.android:id/clips_video_container",
        "com.instagram.android:id/reel_viewer_tall_fragment",
        "com.instagram.android:id/clips_swipe_refresh_container",
        "com.instagram.android:id/clips_viewer_view_pager",
        "com.instagram.android:id/clips_scrim_view"
    )

    override fun detect(rootNode: AccessibilityNodeInfo?): DetectionResult {
        if (rootNode == null) return DetectionResult(false)

        // 1. Check known Reels container View IDs
        for (viewId in reelsViewIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            if (nodes.isNotEmpty()) {
                nodes.forEach { it.recycle() }
                return DetectionResult(
                    isDistractionDetected = true,
                    reason = "Instagram Reels Detectado",
                    details = "Container de visualização de Reels ativo"
                )
            }
        }

        // 2. Inspect selected tabs on the bottom navigation bar
        val tabNodes = rootNode.findAccessibilityNodeInfosByText("Reels")
        for (tab in tabNodes) {
            if (tab.isSelected || tab.parent?.isSelected == true) {
                tab.recycle()
                return DetectionResult(
                    isDistractionDetected = true,
                    reason = "Aba Reels Ativa no Instagram",
                    details = "Navegação na aba dedicada de Reels"
                )
            }
            tab.recycle()
        }

        return DetectionResult(false)
    }
}
