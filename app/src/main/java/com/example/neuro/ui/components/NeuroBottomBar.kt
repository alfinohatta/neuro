package com.example.neuro.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.neuro.ui.navigation.Screen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Focus : BottomNavItem(Screen.Home.route, "Focus", Icons.Default.Timer)
    object Dashboard : BottomNavItem(Screen.Dashboard.route, "Dashboard", Icons.Default.Dashboard)
    object Scan : BottomNavItem(Screen.VitalsScan.route, "Scan", Icons.Default.CenterFocusWeak)
    object Breathing : BottomNavItem(Screen.BreathRelax.route, "Breathing", Icons.Default.SelfImprovement)
    object Insights : BottomNavItem(Screen.Insights.route, "Insights", Icons.Default.BarChart)
}

@Composable
fun NeuroBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Focus,
        BottomNavItem.Dashboard,
        BottomNavItem.Scan,
        BottomNavItem.Breathing,
        BottomNavItem.Insights
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onNavigateToRoute(item.route)
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) }
            )
        }
    }
}
