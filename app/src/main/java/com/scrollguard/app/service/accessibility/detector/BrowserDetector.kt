package com.scrollguard.app.service.accessibility.detector

import android.view.accessibility.AccessibilityNodeInfo

class BrowserDetector {

    companion object {
        val SUPPORTED_BROWSERS = setOf(
            "com.android.chrome",
            "com.brave.browser",
            "org.mozilla.firefox",
            "com.sec.android.app.sbrowser",
            "com.opera.browser",
            "com.microsoft.emmx",
            "com.duckduckgo.mobile.android"
        )
    }

    private val urlBarIds = listOf(
        "com.android.chrome:id/url_bar",
        "com.brave.browser:id/url_bar",
        "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
        "com.sec.android.app.sbrowser:id/location_bar_edit_text",
        "com.opera.browser:id/url_field",
        "com.microsoft.emmx:id/url_bar",
        "com.duckduckgo.mobile.android:id/omnibarTextInput"
    )

    fun isBrowser(packageName: String): Boolean {
        return SUPPORTED_BROWSERS.contains(packageName)
    }

    fun extractUrl(rootNode: AccessibilityNodeInfo?, packageName: String): String? {
        if (rootNode == null) return null

        // 1. Try known view IDs
        for (viewId in urlBarIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            if (nodes.isNotEmpty()) {
                val text = nodes[0].text?.toString()
                nodes.forEach { it.recycle() }
                if (!text.isNullOrBlank()) {
                    return text
                }
            }
        }

        // 2. Generic scan for edit texts that resemble a URL bar
        return findUrlInHierarchy(rootNode)
    }

    private fun findUrlInHierarchy(node: AccessibilityNodeInfo): String? {
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        if (viewId.contains("url") || viewId.contains("omnibar") || viewId.contains("location") || viewId.contains("search_box")) {
            val text = node.text?.toString()
            if (!text.isNullOrBlank()) {
                return text
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findUrlInHierarchy(child)
            child.recycle()
            if (result != null) return result
        }

        return null
    }
}
