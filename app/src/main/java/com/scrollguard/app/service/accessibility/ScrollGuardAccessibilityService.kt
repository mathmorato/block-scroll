package com.scrollguard.app.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.scrollguard.app.core.datastore.UserPreferences
import com.scrollguard.app.core.datastore.UserPreferencesRepository
import com.scrollguard.app.domain.model.BlockType
import com.scrollguard.app.domain.repository.AppRuleRepository
import com.scrollguard.app.domain.usecase.CheckUrlSafetyUseCase
import com.scrollguard.app.domain.usecase.EvaluateScrollSessionUseCase
import com.scrollguard.app.domain.usecase.LogBlockEventUseCase
import com.scrollguard.app.service.accessibility.detector.BrowserDetector
import com.scrollguard.app.service.accessibility.detector.ContentDetector
import com.scrollguard.app.service.accessibility.detector.FacebookDetector
import com.scrollguard.app.service.accessibility.detector.InstagramDetector
import com.scrollguard.app.service.accessibility.detector.TikTokDetector
import com.scrollguard.app.service.accessibility.detector.YouTubeDetector
import com.scrollguard.app.service.overlay.OverlayData
import com.scrollguard.app.service.overlay.OverlayType
import com.scrollguard.app.service.overlay.OverlayWindowManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ScrollGuardAccessibilityService : AccessibilityService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ServiceEntryPoint {
        fun preferencesRepository(): UserPreferencesRepository
        fun appRuleRepository(): AppRuleRepository
        fun logBlockEventUseCase(): LogBlockEventUseCase
        fun checkUrlSafetyUseCase(): CheckUrlSafetyUseCase
        fun evaluateScrollSessionUseCase(): EvaluateScrollSessionUseCase
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var overlayWindowManager: OverlayWindowManager

    private lateinit var preferencesRepo: UserPreferencesRepository
    private lateinit var appRuleRepo: AppRuleRepository
    private lateinit var logBlockEventUseCase: LogBlockEventUseCase
    private lateinit var checkUrlSafetyUseCase: CheckUrlSafetyUseCase
    private lateinit var evaluateScrollSessionUseCase: EvaluateScrollSessionUseCase

    private val detectors = mutableMapOf<String, ContentDetector>()
    private val browserDetector = BrowserDetector()

    private var currentPackage: String = ""
    private var sessionStartTime: Long = 0L
    private var sessionScrollCount: Int = 0
    private var lastInterventionTime: Long = 0L

    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastBlockTimestamp: Long = 0L

    companion object {
        var isServiceRunning = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        overlayWindowManager = OverlayWindowManager(this)

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPoint::class.java
        )
        preferencesRepo = entryPoint.preferencesRepository()
        appRuleRepo = entryPoint.appRuleRepository()
        logBlockEventUseCase = entryPoint.logBlockEventUseCase()
        checkUrlSafetyUseCase = entryPoint.checkUrlSafetyUseCase()
        evaluateScrollSessionUseCase = entryPoint.evaluateScrollSessionUseCase()

        // Register content detectors
        val instagramDetector = InstagramDetector()
        val youTubeDetector = YouTubeDetector()
        val tikTokDetector = TikTokDetector()
        val tikTokGlobalDetector = TikTokDetector("com.ss.android.ugc.trill")
        val fbDetector = FacebookDetector()

        detectors[instagramDetector.supportedPackage] = instagramDetector
        detectors[youTubeDetector.supportedPackage] = youTubeDetector
        detectors[tikTokDetector.supportedPackage] = tikTokDetector
        detectors[tikTokGlobalDetector.supportedPackage] = tikTokGlobalDetector
        detectors[fbDetector.supportedPackage] = fbDetector
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        overlayWindowManager.hideOverlay()
        serviceScope.cancel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: return
        if (pkgName == packageName) {
            // Do not monitor our own application
            return
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(pkgName)
                inspectContent(pkgName)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                inspectContent(pkgName)
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                handleViewScrolled(pkgName)
            }
        }
    }

    override fun onInterrupt() {
        // Called when the system wants to interrupt feedback
    }

    private fun handleWindowStateChanged(packageName: String) {
        if (packageName != currentPackage) {
            currentPackage = packageName
            sessionStartTime = System.currentTimeMillis()
            sessionScrollCount = 0
        }
    }

    private fun handleViewScrolled(packageName: String) {
        sessionScrollCount++
        val now = System.currentTimeMillis()

        // Debounce evaluation every 10 scrolls or at least 15s apart from last intervention
        if (sessionScrollCount % 5 == 0 && (now - lastInterventionTime > 30000L)) {
            val durationMs = now - sessionStartTime
            serviceScope.launch {
                val evaluation = evaluateScrollSessionUseCase(
                    packageName = packageName,
                    sessionDurationMs = durationMs,
                    scrollEventCount = sessionScrollCount
                )

                if (evaluation.shouldTriggerIntervention) {
                    lastInterventionTime = now
                    triggerScrollIntervention(packageName, evaluation.elapsedMinutes, evaluation.delaySeconds)
                }
            }
        }
    }

    private fun inspectContent(packageName: String) {
        val now = System.currentTimeMillis()
        if (now - lastBlockTimestamp < 1500L) {
            // Prevent duplicate triggers within 1.5 seconds
            return
        }

        serviceScope.launch {
            val prefs = preferencesRepo.userPreferencesFlow.first()
            if (!prefs.isGuardianActive) return@launch

            // 1. Check Anti-Reels / Short Video Detectors
            val detector = detectors[packageName]
            if (detector != null && prefs.isAntiReelsActive) {
                val rule = appRuleRepo.getRuleForPackageSync(packageName)
                if (rule == null || (rule.isMonitored && rule.isReelsBlocked)) {
                    val rootNode = rootInActiveWindow
                    if (rootNode != null) {
                        val result = detector.detect(rootNode)
                        rootNode.recycle()
                        if (result.isDistractionDetected) {
                            lastBlockTimestamp = now
                            triggerReelsBlock(packageName, result.reason, result.details)
                            return@launch
                        }
                    }
                }
            }

            // 2. Check Browser URL for Adult Content Blocker
            if (prefs.isAdultFilterActive && browserDetector.isBrowser(packageName)) {
                val rootNode = rootInActiveWindow
                if (rootNode != null) {
                    val url = browserDetector.extractUrl(rootNode, packageName)
                    rootNode.recycle()
                    if (!url.isNullOrBlank()) {
                        val isBlocked = checkUrlSafetyUseCase(url)
                        if (isBlocked) {
                            lastBlockTimestamp = now
                            triggerAdultContentBlock(packageName, url)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    private fun triggerReelsBlock(packageName: String, reason: String, details: String) {
        mainHandler.post {
            // Pull user out of the addictive short-form player
            performGlobalAction(GLOBAL_ACTION_BACK)

            val appName = getFriendlyAppName(packageName)
            serviceScope.launch {
                logBlockEventUseCase(
                    packageName = packageName,
                    appName = appName,
                    blockType = BlockType.REELS,
                    details = "$reason: $details"
                )
            }

            overlayWindowManager.showIntervention(
                data = OverlayData(
                    type = OverlayType.REELS_INTERVENTION,
                    title = "Vídeo Curto Bloqueado!",
                    message = "O ScrollGuard protegeu seu foco contra o algoritmo viciante de vídeos curtos no $appName.",
                    details = reason,
                    countdownSeconds = 3
                ),
                onHomeClicked = {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                },
                onDismissAllowed = {
                    // Allowed to return to previous screen
                }
            )
        }
    }

    private fun triggerScrollIntervention(packageName: String, elapsedMinutes: Int, delaySeconds: Int) {
        mainHandler.post {
            val appName = getFriendlyAppName(packageName)
            serviceScope.launch {
                logBlockEventUseCase(
                    packageName = packageName,
                    appName = appName,
                    blockType = BlockType.SCROLL_LIMIT,
                    details = "Tempo de rolagem contínua: $elapsedMinutes minutos"
                )
            }

            overlayWindowManager.showIntervention(
                data = OverlayData(
                    type = OverlayType.SCROLL_LIMIT_PAUSE,
                    title = "Hora de uma Pausa!",
                    message = "Você está rolando o feed do $appName há $elapsedMinutes minutos sem parar.",
                    details = "A dopamina artificial esgota sua energia mental. Descanse os olhos e retome o controle.",
                    countdownSeconds = delaySeconds
                ),
                onHomeClicked = {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                },
                onDismissAllowed = {
                    // Reset session counter after conscious dismissal
                    sessionStartTime = System.currentTimeMillis()
                    sessionScrollCount = 0
                }
            )
        }
    }

    private fun triggerAdultContentBlock(packageName: String, url: String) {
        mainHandler.post {
            performGlobalAction(GLOBAL_ACTION_BACK)

            val appName = getFriendlyAppName(packageName)
            serviceScope.launch {
                logBlockEventUseCase(
                    packageName = packageName,
                    appName = appName,
                    blockType = BlockType.ADULT_CONTENT,
                    details = "Domínio adulto bloqueado: $url"
                )
            }

            overlayWindowManager.showIntervention(
                data = OverlayData(
                    type = OverlayType.ADULT_RESTRICTION,
                    title = "Conteúdo Adulto Bloqueado",
                    message = "O filtro de proteção do ScrollGuard impediu o acesso a este endereço restrito.",
                    details = "Mantenha sua disciplina e foco no que realmente importa.",
                    countdownSeconds = 3
                ),
                onHomeClicked = {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                },
                onDismissAllowed = {
                    // Return safely
                }
            )
        }
    }

    private fun getFriendlyAppName(pkg: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            when (pkg) {
                "com.instagram.android" -> "Instagram"
                "com.google.android.youtube" -> "YouTube"
                "com.zhiliaoapp.musically", "com.ss.android.ugc.trill" -> "TikTok"
                "com.facebook.katana" -> "Facebook"
                "com.twitter.android" -> "X (Twitter)"
                "com.android.chrome" -> "Chrome"
                else -> pkg
            }
        }
    }
}
