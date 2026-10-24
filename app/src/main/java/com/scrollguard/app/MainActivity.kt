package com.scrollguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.scrollguard.app.core.datastore.UserPreferencesRepository
import com.scrollguard.app.core.util.PermissionHelper
import com.scrollguard.app.presentation.navigation.NavGraph
import com.scrollguard.app.presentation.navigation.Screen
import com.scrollguard.app.presentation.theme.ScrollGuardTheme
import com.scrollguard.app.service.accessibility.ScrollGuardAccessibilityService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ScrollGuardTheme {
                val preferences by preferencesRepository.userPreferencesFlow.collectAsState(initial = null)
                val navController = rememberNavController()

                val isServiceActive = ScrollGuardAccessibilityService.isServiceRunning &&
                        PermissionHelper.isAccessibilityServiceEnabled(this)
                val hasOverlay = PermissionHelper.canDrawOverlays(this)

                // Decide start screen: if onboarding not completed or missing core permissions, show onboarding
                val startDestination = if (preferences?.isOnboardingCompleted == true && isServiceActive && hasOverlay) {
                    Screen.Dashboard.route
                } else {
                    Screen.Onboarding.route
                }

                NavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}
