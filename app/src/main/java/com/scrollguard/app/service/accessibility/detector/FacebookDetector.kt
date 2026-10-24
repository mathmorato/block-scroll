package com.scrollguard.app.service.accessibility.detector

import android.view.accessibility.AccessibilityNodeInfo

class FacebookDetector : ContentDetector {
    override val supportedPackage: String = "com.facebook.katana"

    private val fbReelsViewIds = listOf(
        "com.facebook.katana:id/fb_reels_container",
        "com.facebook.katana:id/reels_tray",
        "com.facebook.katana:id/reels_viewer_activity",
        "com.facebook.katana:id/shorts_container"
    )

    override fun detect(rootNode: AccessibilityNodeInfo?): DetectionResult {
        if (rootNode == null) return DetectionResult(false)

        for (viewId in fbReelsViewIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            if (nodes.isNotEmpty()) {
                nodes.forEach { it.recycle() }
                return DetectionResult(
                    isDistractionDetected = true,
                    reason = "Facebook Reels Detectado",
                    details = "Player de vídeo Reels no Facebook ativo"
                )
            }
        }

        val textNodes = rootNode.findAccessibilityNodeInfosByText("Reels")
        for (node in textNodes) {
            if (node.isSelected || node.parent?.isSelected == true) {
                node.recycle()
                return DetectionResult(
                    isDistractionDetected = true,
                    reason = "Aba de Reels no Facebook",
                    details = "Navegação na aba de vídeos rápidos"
                )
            }
            node.recycle()
        }

        return DetectionResult(false)
    }
}
