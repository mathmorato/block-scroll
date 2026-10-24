package com.scrollguard.app.presentation.rules

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scrollguard.app.domain.model.AppRule
import com.scrollguard.app.domain.repository.AppRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RulesUiState(
    val rules: List<AppRule> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class RulesViewModel @Inject constructor(
    application: Application,
    private val appRuleRepository: AppRuleRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RulesUiState(isLoading = true))
    val uiState: StateFlow<RulesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appRuleRepository.insertDefaultRulesIfEmpty()
            scanInstalledApps()
            appRuleRepository.getAllRules().collect { rulesList ->
                _uiState.value = _uiState.value.copy(
                    rules = rulesList,
                    isLoading = false
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateRule(rule: AppRule) {
        viewModelScope.launch {
            appRuleRepository.upsertRule(rule)
        }
    }

    private fun scanInstalledApps() {
        viewModelScope.launch {
            val pm = getApplication<Application>().packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (pkg == getApplication<Application>().packageName) continue

                val existing = appRuleRepository.getRuleForPackageSync(pkg)
                if (existing == null) {
                    val label = info.loadLabel(pm).toString()
                    val isSocial = pkg.contains("instagram") || pkg.contains("youtube") ||
                            pkg.contains("tiktok") || pkg.contains("facebook") ||
                            pkg.contains("twitter") || pkg.contains("chrome")

                    if (isSocial) {
                        appRuleRepository.upsertRule(
                            AppRule(
                                packageName = pkg,
                                appName = label,
                                isMonitored = true,
                                isReelsBlocked = true,
                                isScrollLimitBlocked = true,
                                customScrollLimitMinutes = 10
                            )
                        )
                    }
                }
            }
        }
    }
}
