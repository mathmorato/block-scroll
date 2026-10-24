package com.scrollguard.app.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scrollguard.app.core.datastore.UserPreferences
import com.scrollguard.app.core.datastore.UserPreferencesRepository
import com.scrollguard.app.core.util.PermissionHelper
import com.scrollguard.app.domain.model.BlockLog
import com.scrollguard.app.domain.model.GuardianStatus
import com.scrollguard.app.domain.model.StatsSummary
import com.scrollguard.app.domain.repository.BlockLogRepository
import com.scrollguard.app.domain.usecase.GetDashboardStatsUseCase
import com.scrollguard.app.service.accessibility.ScrollGuardAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val guardianStatus: GuardianStatus = GuardianStatus(false, false, false, false),
    val preferences: UserPreferences = UserPreferences(),
    val stats: StatsSummary = StatsSummary(),
    val recentLogs: List<BlockLog> = emptyList(),
    val showStrictChallengeDialog: Boolean = false,
    val pendingToggleAction: (() -> Unit)? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    private val preferencesRepository: UserPreferencesRepository,
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val blockLogRepository: BlockLogRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesRepository.userPreferencesFlow,
                getDashboardStatsUseCase(),
                blockLogRepository.getRecentLogs(10)
            ) { prefs, stats, logs ->
                val context = getApplication<Application>()
                val isServiceActive = ScrollGuardAccessibilityService.isServiceRunning &&
                        PermissionHelper.isAccessibilityServiceEnabled(context)
                val hasOverlay = PermissionHelper.canDrawOverlays(context)
                val isBatteryIgnored = PermissionHelper.isIgnoringBatteryOptimizations(context)

                val status = GuardianStatus(
                    isServiceEnabled = isServiceActive,
                    isOverlayPermissionGranted = hasOverlay,
                    isBatteryOptimizationIgnored = isBatteryIgnored,
                    isProtectionActive = prefs.isGuardianActive && isServiceActive
                )

                _uiState.value.copy(
                    guardianStatus = status,
                    preferences = prefs,
                    stats = stats,
                    recentLogs = logs
                )
            }.collect { updated ->
                _uiState.value = updated
            }
        }
    }

    fun checkStatus() {
        val context = getApplication<Application>()
        val isServiceActive = ScrollGuardAccessibilityService.isServiceRunning &&
                PermissionHelper.isAccessibilityServiceEnabled(context)
        val hasOverlay = PermissionHelper.canDrawOverlays(context)
        val isBatteryIgnored = PermissionHelper.isIgnoringBatteryOptimizations(context)

        val current = _uiState.value
        _uiState.value = current.copy(
            guardianStatus = GuardianStatus(
                isServiceEnabled = isServiceActive,
                isOverlayPermissionGranted = hasOverlay,
                isBatteryOptimizationIgnored = isBatteryIgnored,
                isProtectionActive = current.preferences.isGuardianActive && isServiceActive
            )
        )
    }

    fun toggleGuardian(active: Boolean) {
        checkStrictAndExecute {
            viewModelScope.launch {
                preferencesRepository.setGuardianActive(active)
            }
        }
    }

    fun toggleAntiReels(active: Boolean) {
        checkStrictAndExecute {
            viewModelScope.launch {
                preferencesRepository.setAntiReelsActive(active)
            }
        }
    }

    fun toggleAntiScroll(active: Boolean) {
        checkStrictAndExecute {
            viewModelScope.launch {
                preferencesRepository.setAntiScrollActive(active)
            }
        }
    }

    fun toggleAdultFilter(active: Boolean) {
        checkStrictAndExecute {
            viewModelScope.launch {
                preferencesRepository.setAdultFilterActive(active)
            }
        }
    }

    private fun checkStrictAndExecute(action: () -> Unit) {
        val prefs = _uiState.value.preferences
        val now = System.currentTimeMillis()
        val isStrictLocked = prefs.isStrictModeActive && (prefs.strictModeUntilTimestamp == 0L || prefs.strictModeUntilTimestamp > now)

        if (isStrictLocked) {
            // Strict mode is active! Require challenge dialog before allowing changes
            _uiState.value = _uiState.value.copy(
                showStrictChallengeDialog = true,
                pendingToggleAction = action
            )
        } else {
            action()
        }
    }

    fun dismissChallengeDialog() {
        _uiState.value = _uiState.value.copy(
            showStrictChallengeDialog = false,
            pendingToggleAction = null
        )
    }

    fun onChallengeCompleted() {
        val action = _uiState.value.pendingToggleAction
        _uiState.value = _uiState.value.copy(
            showStrictChallengeDialog = false,
            pendingToggleAction = null
        )
        action?.invoke()
    }
}
