package com.example.neuro.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Dashboard : Screen("dashboard")
    object VitalsScan : Screen("vitals_scan")
    object ActiveSession : Screen("active_session/{title}/{duration}/{type}") {
        fun createRoute(title: String, duration: Int, type: String) =
            "active_session/${android.net.Uri.encode(title)}/$duration/${android.net.Uri.encode(type)}"
    }
    object BreathingExercise : Screen("breathing_exercise/{exerciseId}") {
        fun createRoute(exerciseId: String = "478_reset") = "breathing_exercise/$exerciseId"
    }
    object BreathRelax : Screen("breath_relax")
    object Insights : Screen("insights")
    object Profile : Screen("profile")
}
