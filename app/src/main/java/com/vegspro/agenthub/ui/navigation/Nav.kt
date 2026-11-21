package com.vegspro.agenthub.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vegspro.agenthub.ui.screens.AgentsScreen
import com.vegspro.agenthub.ui.screens.CatalogScreen
import com.vegspro.agenthub.ui.screens.ChatScreen
import com.vegspro.agenthub.ui.screens.SettingsScreen
import com.vegspro.agenthub.viewmodel.ChatViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Chat : Screen("chat", "Chat", Icons.Default.Chat)
    data object Catalog : Screen("catalog", "Models", Icons.Default.Download)
    data object Agents : Screen("agents", "Agents", Icons.Default.SmartToy)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val screens = listOf(Screen.Chat, Screen.Catalog, Screen.Agents, Screen.Settings)

@Composable
fun AgentHubNav() {
    val navController = rememberNavController()
    val chatViewModel: ChatViewModel = viewModel()

    Scaffold(
        topBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Chat.route) {
                ChatScreen(viewModel = chatViewModel)
            }
            composable(Screen.Catalog.route) {
                CatalogScreen()
            }
            composable(Screen.Agents.route) {
                AgentsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = chatViewModel)
            }
        }
    }
}
