package com.scrollguard.app.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    data object Onboarding : Screen("onboarding", "Boas-vindas")
    data object OemGuide : Screen("oem_guide", "Guia do Fabricante")
    data object Dashboard : Screen("dashboard", "Painel")
    data object Rules : Screen("rules", "Aplicativos")
    data object Stats : Screen("stats", "Estatísticas")
    data object StrictMode : Screen("strict_mode", "Modo Estrito")
}
