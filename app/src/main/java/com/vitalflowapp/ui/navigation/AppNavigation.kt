package com.vitalflowapp.ui.navigation

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
import com.vitalflowapp.ui.screens.addreading.AddReadingScreen
import com.vitalflowapp.ui.screens.articles.ArticleDetailScreen
import com.vitalflowapp.ui.screens.articles.ArticlesScreen
import com.vitalflowapp.ui.screens.breathing.BreathingExerciseScreen
import com.vitalflowapp.ui.screens.glucose.GlucoseScreen
import com.vitalflowapp.ui.screens.goals.GoalsScreen
import com.vitalflowapp.ui.screens.history.HistoryScreen
import com.vitalflowapp.ui.screens.home.HomeScreen
import com.vitalflowapp.ui.screens.insights.InsightsScreen
import com.vitalflowapp.ui.screens.medication.MedicationScreen
import com.vitalflowapp.ui.screens.profile.ProfileScreen
import com.vitalflowapp.ui.screens.quickentry.QuickEntryScreen
import com.vitalflowapp.ui.screens.relaxation.WhiteCoatHelperScreen
import com.vitalflowapp.ui.screens.reminder.ReminderScreen
import com.vitalflowapp.ui.screens.settings.SettingsScreen
import com.vitalflowapp.ui.screens.statistics.StatisticsScreen
import com.vitalflowapp.ui.screens.voice.VoiceInputScreen
import com.vitalflowapp.ui.screens.weight.WeightScreen
import com.vitalflowapp.ui.screens.bluetooth.BluetoothScreen
import com.vitalflowapp.ui.screens.emergency.EmergencyScreen
import com.vitalflowapp.ui.screens.healthconnect.HealthConnectScreen

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
    object QuickEntry : Screen("quick_entry", "Quick Entry", Icons.Filled.Speed, Icons.Outlined.Speed)
    object VoiceInput : Screen("voice_input", "Voice Input", Icons.Filled.Mic, Icons.Outlined.Mic)
    object Medications : Screen("medications", "Medications", Icons.Filled.Medication, Icons.Outlined.Medication)
    object Profiles : Screen("profiles", "Profiles", Icons.Filled.People, Icons.Outlined.People)
    object Breathing : Screen("breathing", "Breathing", Icons.Filled.Air, Icons.Outlined.Air)
    object WhiteCoatHelper : Screen("white_coat_helper", "Relax", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement)
    object Weight : Screen("weight", "Weight", Icons.Filled.MonitorWeight, Icons.Outlined.MonitorWeight)
    object Glucose : Screen("glucose", "Glucose", Icons.Filled.Bloodtype, Icons.Outlined.Bloodtype)
    object Goals : Screen("goals", "Goals", Icons.Filled.Flag, Icons.Outlined.Flag)
    object Insights : Screen("insights", "Insights", Icons.Filled.Insights, Icons.Outlined.Insights)
    object Bluetooth : Screen("bluetooth", "Bluetooth", Icons.Filled.Bluetooth, Icons.Outlined.Bluetooth)
    object Emergency : Screen("emergency", "Emergency", Icons.Filled.Emergency, Icons.Outlined.Emergency)
    object HealthConnect : Screen("health_connect", "Health Connect", Icons.Filled.HealthAndSafety, Icons.Outlined.HealthAndSafety)
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
                    onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                    onNavigateToMedications = { navController.navigate(Screen.Medications.route) },
                    onNavigateToProfiles = { navController.navigate(Screen.Profiles.route) },
                    onNavigateToBreathing = { navController.navigate(Screen.Breathing.route) },
                    onNavigateToWeight = { navController.navigate(Screen.Weight.route) },
                    onNavigateToGlucose = { navController.navigate(Screen.Glucose.route) },
                    onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                    onNavigateToInsights = { navController.navigate(Screen.Insights.route) },
                    onNavigateToBluetooth = { navController.navigate(Screen.Bluetooth.route) },
                    onNavigateToEmergency = { navController.navigate(Screen.Emergency.route) },
                    onNavigateToHealthConnect = { navController.navigate(Screen.HealthConnect.route) }
                )
            }
            
            composable(
                route = "add_reading?systolic={systolic}&diastolic={diastolic}&pulse={pulse}",
                arguments = listOf(
                    navArgument("systolic") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("diastolic") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("pulse") { type = NavType.IntType; defaultValue = 0 }
                )
            ) { backStackEntry ->
                val systolic = backStackEntry.arguments?.getInt("systolic") ?: 0
                val diastolic = backStackEntry.arguments?.getInt("diastolic") ?: 0
                val pulse = backStackEntry.arguments?.getInt("pulse") ?: 0
                AddReadingScreen(
                    onNavigateBack = { navController.popBackStack() },
                    initialSystolic = systolic,
                    initialDiastolic = diastolic,
                    initialPulse = pulse
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
            
            composable(Screen.QuickEntry.route) {
                QuickEntryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onExpandToFull = { 
                        navController.popBackStack()
                        navController.navigate(Screen.AddReading.route)
                    }
                )
            }
            
            composable(Screen.VoiceInput.route) {
                VoiceInputScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Medications.route) {
                MedicationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Profiles.route) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Breathing.route) {
                BreathingExerciseScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.WhiteCoatHelper.route) {
                WhiteCoatHelperScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStartMeasurement = {
                        navController.popBackStack()
                        navController.navigate(Screen.AddReading.route)
                    }
                )
            }
            
            composable(Screen.Weight.route) {
                WeightScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Glucose.route) {
                GlucoseScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Goals.route) {
                GoalsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Insights.route) {
                InsightsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Bluetooth.route) {
                BluetoothScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onReadingReceived = { systolic, diastolic, pulse ->
                        navController.popBackStack()
                        navController.navigate("add_reading?systolic=$systolic&diastolic=$diastolic&pulse=$pulse")
                    }
                )
            }
            
            composable(Screen.Emergency.route) {
                EmergencyScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.HealthConnect.route) {
                HealthConnectScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
