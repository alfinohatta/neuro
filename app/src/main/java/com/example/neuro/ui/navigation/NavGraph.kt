package com.example.neuro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.neuro.ui.screens.*
import com.example.neuro.ui.viewmodel.FocusSessionViewModel

@Composable
fun NeuroNavGraph(
    navController: NavHostController,
    sessionViewModel: FocusSessionViewModel = viewModel()
) {
    val navigateToRoute: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Screen.Home.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateBack: () -> Unit = {
        if (!navController.popBackStack()) {
            navController.navigate(Screen.Home.route) {
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onContinueToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onStartSession = { title, duration, type ->
                    sessionViewModel.startSession(title, duration, type)
                    navController.navigate(Screen.ActiveSession.createRoute(title, duration, type))
                },
                onNavigateBack = navigateBack,
                onNavigateToRoute = navigateToRoute,
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateBack = navigateBack,
                onNavigateToRoute = navigateToRoute,
                onNavigateToScan = { navController.navigate(Screen.VitalsScan.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.VitalsScan.route) {
            VitalsScanScreen(
                onNavigateBack = navigateBack,
                onNavigateToRoute = navigateToRoute,
                onNavigateToInsights = { navController.navigate(Screen.Insights.route) }
            )
        }

        composable(
            route = Screen.ActiveSession.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType },
                navArgument("type") { type = NavType.StringType }
            )
        ) {
            ActiveSessionScreen(
                viewModel = sessionViewModel,
                onNavigateBack = navigateBack
            )
        }

        composable(Screen.BreathRelax.route) {
            BreathRelaxScreen(
                onNavigateBack = navigateBack,
                onNavigateToRoute = navigateToRoute,
                onStartBreathingExercise = { exerciseId ->
                    navController.navigate(Screen.BreathingExercise.createRoute(exerciseId))
                }
            )
        }

        composable(
            route = Screen.BreathingExercise.route,
            arguments = listOf(
                navArgument("exerciseId") {
                    type = NavType.StringType
                    defaultValue = "478_reset"
                }
            )
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: "478_reset"
            BreathingExerciseScreen(
                exerciseId = exerciseId,
                onNavigateBack = navigateBack
            )
        }

        composable(Screen.Insights.route) {
            InsightsScreen(
                onNavigateBack = navigateBack,
                onNavigateToRoute = navigateToRoute
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = navigateBack,
                onNavigateToRoute = navigateToRoute
            )
        }
    }
}
