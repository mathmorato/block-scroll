package com.scrollguard.app.service.accessibility.detector

import android.view.accessibility.AccessibilityNodeInfo

class TikTokDetector(override val supportedPackage: String = "com.zhiliaoapp.musically") : ContentDetector {

    private val tikTokViewIds = listOf(
        "$supportedPackage:id/viewpager",
        "$supportedPackage:id/vertical_viewpager",
        "$supportedPackage:id/main_content",
        "$supportedPackage:id/feed_viewpager"
    )

    override fun detect(rootNode: AccessibilityNodeInfo?): DetectionResult {
        if (rootNode == null) return DetectionResult(false)

        for (viewId in tikTokViewIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            if (nodes.isNotEmpty()) {
                nodes.forEach { it.recycle() }
                return DetectionResult(
                    isDistractionDetected = true,
                    reason = "TikTok Vídeo Infinito Detectado",
                    details = "Feed vertical do TikTok ativo"
                )
            }
        }

        // TikTok is virtually 100% short videos; if user configured to block it, any root container triggers detection
        return DetectionResult(
            isDistractionDetected = true,
            reason = "TikTok em Execução",
            details = "Bloqueio do aplicativo de vídeo curto"
        )
    }
}
