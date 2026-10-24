package com.scrollguard.app.presentation.strictmode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollguard.app.core.datastore.UserPreferences
import com.scrollguard.app.core.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StrictModeUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isTimerActive: Boolean = false,
    val remainingLockHours: Int = 0,
    val selectedLockDurationHours: Int = 24
)

@HiltViewModel
class StrictModeViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StrictModeUiState())
    val uiState: StateFlow<StrictModeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                val now = System.currentTimeMillis()
                val isLocked = prefs.isStrictModeActive && (prefs.strictModeUntilTimestamp == 0L || prefs.strictModeUntilTimestamp > now)
                val remainingHours = if (prefs.strictModeUntilTimestamp > now) {
                    ((prefs.strictModeUntilTimestamp - now) / 3600000).toInt() + 1
                } else 0

                _uiState.value = _uiState.value.copy(
                    preferences = prefs,
                    isTimerActive = isLocked,
                    remainingLockHours = remainingHours
                )
            }
        }
    }

    fun setLockDuration(hours: Int) {
        _uiState.value = _uiState.value.copy(selectedLockDurationHours = hours)
    }

    fun setChallengeType(type: String, pin: String = "") {
        viewModelScope.launch {
            preferencesRepository.setStrictModeChallenge(type, pin)
        }
    }

    fun activateStrictMode(hours: Int) {
        viewModelScope.launch {
            val untilTimestamp = if (hours > 0) {
                System.currentTimeMillis() + (hours * 3600000L)
            } else {
                0L // Indefinite until manual conscious challenge
            }
            preferencesRepository.setStrictModeActive(true, untilTimestamp)
        }
    }

    fun deactivateStrictMode() {
        viewModelScope.launch {
            preferencesRepository.setStrictModeActive(false, 0L)
        }
    }
}
