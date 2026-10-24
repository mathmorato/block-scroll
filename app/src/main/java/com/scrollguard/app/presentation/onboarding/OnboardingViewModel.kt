package com.scrollguard.app.presentation.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scrollguard.app.core.datastore.UserPreferencesRepository
import com.scrollguard.app.core.util.ManufacturerHelper
import com.scrollguard.app.core.util.OemBrand
import com.scrollguard.app.core.util.OemInstruction
import com.scrollguard.app.core.util.PermissionHelper
import com.scrollguard.app.service.accessibility.ScrollGuardAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val isAccessibilityGranted: Boolean = false,
    val isOverlayGranted: Boolean = false,
    val isBatteryIgnored: Boolean = false,
    val oemBrand: OemBrand = OemBrand.OTHER,
    val brandName: String = "",
    val oemInstructions: List<OemInstruction> = emptyList(),
    val isCompleted: Boolean = false
) {
    val areCorePermissionsGranted: Boolean
        get() = isAccessibilityGranted && isOverlayGranted
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    application: Application,
    private val preferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        val brand = ManufacturerHelper.getDeviceBrand()
        val brandName = ManufacturerHelper.getBrandName()
        val instructions = ManufacturerHelper.getOemInstructions(brand)
        _uiState.value = _uiState.value.copy(
            oemBrand = brand,
            brandName = brandName,
            oemInstructions = instructions
        )
        refreshPermissions()
    }

    fun refreshPermissions() {
        val context = getApplication<Application>()
        val hasAccessibility = PermissionHelper.isAccessibilityServiceEnabled(context) ||
                ScrollGuardAccessibilityService.isServiceRunning
        val hasOverlay = PermissionHelper.canDrawOverlays(context)
        val isBatteryIgnored = PermissionHelper.isIgnoringBatteryOptimizations(context)

        _uiState.value = _uiState.value.copy(
            isAccessibilityGranted = hasAccessibility,
            isOverlayGranted = hasOverlay,
            isBatteryIgnored = isBatteryIgnored
        )
    }

    fun requestAccessibility() {
        PermissionHelper.openAccessibilitySettings(getApplication())
    }

    fun requestOverlay() {
        PermissionHelper.openOverlaySettings(getApplication())
    }

    fun requestIgnoreBattery() {
        PermissionHelper.requestIgnoreBatteryOptimization(getApplication())
    }

    fun openOemAutostartSettings() {
        ManufacturerHelper.openAutostartSettings(getApplication())
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
            _uiState.value = _uiState.value.copy(isCompleted = true)
        }
    }
}
