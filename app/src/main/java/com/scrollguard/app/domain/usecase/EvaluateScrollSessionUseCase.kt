package com.scrollguard.app.domain.usecase

import com.scrollguard.app.core.datastore.UserPreferencesRepository
import com.scrollguard.app.domain.repository.AppRuleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class ScrollEvaluationResult(
    val shouldTriggerIntervention: Boolean,
    val elapsedMinutes: Int,
    val limitMinutes: Int,
    val delaySeconds: Int
)

class EvaluateScrollSessionUseCase @Inject constructor(
    private val appRuleRepository: AppRuleRepository,
    private val preferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(
        packageName: String,
        sessionDurationMs: Long,
        scrollEventCount: Int
    ): ScrollEvaluationResult {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        if (!prefs.isGuardianActive || !prefs.isAntiScrollActive) {
            return ScrollEvaluationResult(false, 0, 0, 0)
        }

        val rule = appRuleRepository.getRuleForPackageSync(packageName)
        if (rule != null && (!rule.isMonitored || !rule.isScrollLimitBlocked)) {
            return ScrollEvaluationResult(false, 0, 0, 0)
        }

        val limitMinutes = rule?.customScrollLimitMinutes ?: prefs.scrollSessionLimitMinutes
        val elapsedMinutes = (sessionDurationMs / 60000).toInt()

        // Trigger intervention if user exceeds continuous time limit OR excessive rapid scrolls (e.g., 120 swipes in session)
        val shouldIntervene = elapsedMinutes >= limitMinutes || (scrollEventCount >= 150 && elapsedMinutes >= 3)

        return ScrollEvaluationResult(
            shouldTriggerIntervention = shouldIntervene,
            elapsedMinutes = elapsedMinutes,
            limitMinutes = limitMinutes,
            delaySeconds = prefs.scrollDelaySeconds
        )
    }
}
