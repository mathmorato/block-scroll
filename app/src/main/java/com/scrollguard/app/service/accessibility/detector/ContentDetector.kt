package com.scrollguard.app.service.accessibility.detector

import android.view.accessibility.AccessibilityNodeInfo

data class DetectionResult(
    val isDistractionDetected: Boolean,
    val reason: String = "",
    val details: String = ""
)

interface ContentDetector {
    val supportedPackage: String
    fun detect(rootNode: AccessibilityNodeInfo?): DetectionResult
}
