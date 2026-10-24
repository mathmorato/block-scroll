package com.scrollguard.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.scrollguard.app.presentation.common.AppBottomNavigation
import com.scrollguard.app.presentation.dashboard.DashboardScreen
import com.scrollguard.app.presentation.dashboard.DashboardViewModel
import com.scrollguard.app.presentation.onboarding.OemGuideScreen
import com.scrollguard.app.presentation.onboarding.OnboardingScreen
import com.scrollguard.app.presentation.onboarding.OnboardingViewModel
import com.scrollguard.app.presentation.rules.RulesScreen
import com.scrollguard.app.presentation.rules.RulesViewModel
import com.scrollguard.app.presentation.stats.StatsScreen
import com.scrollguard.app.presentation.stats.StatsViewModel
import com.scrollguard.app.presentation.strictmode.StrictModeScreen
import com.scrollguard.app.presentation.strictmode.StrictModeViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination

    val mainTabs = listOf(
        Screen.Dashboard.route,
        Screen.Rules.route,
        Screen.Stats.route,
        Screen.StrictMode.route
    )
    val showBottomBar = currentRoute in mainTabs

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Screen.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    onNavigateToOemGuide = {
                        navController.navigate(Screen.OemGuide.route)
                    }
                )
            }

            composable(Screen.OemGuide.route) {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                OemGuideScreen(
                    viewModel = onboardingViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route)
                    },
                    onNavigateToOemGuide = {
                        navController.navigate(Screen.OemGuide.route)
                    },
                    onNavigateToStrictMode = {
                        navController.navigate(Screen.StrictMode.route)
                    }
                )
            }

            composable(Screen.Rules.route) {
                val rulesViewModel: RulesViewModel = hiltViewModel()
                RulesScreen(viewModel = rulesViewModel)
            }

            composable(Screen.Stats.route) {
                val statsViewModel: StatsViewModel = hiltViewModel()
                StatsScreen(viewModel = statsViewModel)
            }

            composable(Screen.StrictMode.route) {
                val strictModeViewModel: StrictModeViewModel = hiltViewModel()
                StrictModeScreen(viewModel = strictModeViewModel)
            }
        }
    }
}
