package com.adguard.wireguardhotspotbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adguard.wireguardhotspotbridge.ui.screens.adb.AdbBoostScreen
import com.adguard.wireguardhotspotbridge.ui.screens.control.ControlScreen
import com.adguard.wireguardhotspotbridge.ui.screens.diagnostics.DiagnosticsScreen
import com.adguard.wireguardhotspotbridge.ui.screens.profile.ProfileScreen
import com.adguard.wireguardhotspotbridge.ui.theme.AppTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomItem(Route.Profile, "Профиль"),
        BottomItem(Route.Control, "Управление"),
        BottomItem(Route.Diagnostics, "Диагностика"),
        BottomItem(Route.AdbBoost, "ADB"),
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route.value } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route.value) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(item.label) },
                        icon = { Text(item.iconText) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Control.value,
            modifier = Modifier,
        ) {
            composable(Route.Profile.value) { ProfileScreen(contentPadding = padding) }
            composable(Route.Control.value) { ControlScreen(contentPadding = padding) }
            composable(Route.Diagnostics.value) { DiagnosticsScreen(contentPadding = padding) }
            composable(Route.AdbBoost.value) { AdbBoostScreen(contentPadding = padding) }
        }
    }
}

private data class BottomItem(val route: Route, val label: String) {
    val iconText: String =
        when (route) {
            Route.Profile -> "WG"
            Route.Control -> "ON"
            Route.Diagnostics -> "i"
            Route.AdbBoost -> "ADB"
        }
}

private enum class Route(val value: String) {
    Profile("profile"),
    Control("control"),
    Diagnostics("diagnostics"),
    AdbBoost("adb_boost"),
}

