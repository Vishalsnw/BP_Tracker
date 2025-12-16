package com.bptracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bptracker.ui.screens.addreading.AddReadingScreen
import com.bptracker.ui.screens.articles.ArticleDetailScreen
import com.bptracker.ui.screens.articles.ArticlesScreen
import com.bptracker.ui.screens.history.HistoryScreen
import com.bptracker.ui.screens.home.HomeScreen
import com.bptracker.ui.screens.reminder.ReminderScreen
import com.bptracker.ui.screens.settings.SettingsScreen
import com.bptracker.ui.screens.statistics.StatisticsScreen

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Statistics : Screen("statistics", "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Articles : Screen("articles", "Learn", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    object AddReading : Screen("add_reading", "Add Reading", Icons.Filled.Add, Icons.Outlined.Add)
    object EditReading : Screen("edit_reading/{readingId}", "Edit Reading", Icons.Filled.Edit, Icons.Outlined.Edit)
    object Reminders : Screen("reminders", "Reminders", Icons.Filled.Alarm, Icons.Outlined.Alarm)
    object ArticleDetail : Screen("article/{articleId}", "Article", Icons.Filled.Article, Icons.Outlined.Article)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.History,
    Screen.Statistics,
    Screen.Articles,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showBottomBar = bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onAddReading = { navController.navigate(Screen.AddReading.route) },
                    onViewHistory = { navController.navigate(Screen.History.route) },
                    onEditReading = { id -> navController.navigate("edit_reading/$id") }
                )
            }
            
            composable(Screen.History.route) {
                HistoryScreen(
                    onEditReading = { id -> navController.navigate("edit_reading/$id") },
                    onAddReading = { navController.navigate(Screen.AddReading.route) }
                )
            }
            
            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }
            
            composable(Screen.Articles.route) {
                ArticlesScreen(
                    onArticleClick = { id -> navController.navigate("article/$id") }
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToReminders = { navController.navigate(Screen.Reminders.route) }
                )
            }
            
            composable(Screen.AddReading.route) {
                AddReadingScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.EditReading.route,
                arguments = listOf(navArgument("readingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val readingId = backStackEntry.arguments?.getLong("readingId") ?: 0L
                AddReadingScreen(
                    readingId = readingId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Reminders.route) {
                ReminderScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.ArticleDetail.route,
                arguments = listOf(navArgument("articleId") { type = NavType.IntType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getInt("articleId") ?: 0
                ArticleDetailScreen(
                    articleId = articleId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
