package com.bptracker.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToReminders: () -> Unit,
    onNavigateToMedications: () -> Unit = {},
    onNavigateToProfiles: () -> Unit = {},
    onNavigateToBreathing: () -> Unit = {},
    onNavigateToWeight: () -> Unit = {},
    onNavigateToGlucose: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToBluetooth: () -> Unit = {},
    onNavigateToEmergency: () -> Unit = {},
    onNavigateToHealthConnect: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            title = { Text("Delete All Data") },
            text = { 
                Text("Are you sure you want to delete all readings? This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
            title = { Text("Blood Pressure Tracker") },
            text = {
                Column {
                    Text("Version 1.0.0")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Track your blood pressure readings and monitor your heart health with detailed statistics and insights.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "This app is for informational purposes only and is not a substitute for professional medical advice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "Profiles") {
                SettingsItem(
                    icon = Icons.Filled.People,
                    title = "Family Profiles",
                    subtitle = "Manage profiles for multiple family members",
                    onClick = onNavigateToProfiles
                )
            }
            
            SettingsSection(title = "Health Tracking") {
                SettingsItem(
                    icon = Icons.Filled.MonitorWeight,
                    title = "Weight Tracking",
                    subtitle = "Track weight and BMI",
                    onClick = onNavigateToWeight
                )
                SettingsItem(
                    icon = Icons.Filled.Bloodtype,
                    title = "Blood Glucose",
                    subtitle = "Monitor blood sugar levels",
                    onClick = onNavigateToGlucose
                )
                SettingsItem(
                    icon = Icons.Filled.Medication,
                    title = "Medications",
                    subtitle = "Track your blood pressure medications",
                    onClick = onNavigateToMedications
                )
                SettingsItem(
                    icon = Icons.Filled.Air,
                    title = "Breathing Exercises",
                    subtitle = "Guided relaxation to help lower blood pressure",
                    onClick = onNavigateToBreathing
                )
            }
            
            SettingsSection(title = "Goals & Insights") {
                SettingsItem(
                    icon = Icons.Filled.Flag,
                    title = "Health Goals",
                    subtitle = "Set and track your health targets",
                    onClick = onNavigateToGoals
                )
                SettingsItem(
                    icon = Icons.Filled.Insights,
                    title = "Insights & Analytics",
                    subtitle = "Personalized health insights based on your data",
                    onClick = onNavigateToInsights
                )
            }
            
            SettingsSection(title = "Devices & Sync") {
                SettingsItem(
                    icon = Icons.Filled.Bluetooth,
                    title = "Bluetooth BP Monitor",
                    subtitle = "Connect to compatible Bluetooth monitors",
                    onClick = onNavigateToBluetooth
                )
                SettingsItem(
                    icon = Icons.Filled.HealthAndSafety,
                    title = "Health Connect",
                    subtitle = "Sync with Android Health Connect",
                    onClick = onNavigateToHealthConnect
                )
            }
            
            SettingsSection(title = "Safety") {
                SettingsItem(
                    icon = Icons.Filled.Emergency,
                    title = "Crisis Response",
                    subtitle = "Set up emergency contacts and alerts",
                    onClick = onNavigateToEmergency
                )
            }
            
            SettingsSection(title = "Notifications") {
                SettingsItem(
                    icon = Icons.Filled.Alarm,
                    title = "Measurement Reminders",
                    subtitle = "Set up reminders to measure your blood pressure",
                    onClick = onNavigateToReminders
                )
            }
            
            SettingsSection(title = "Data") {
                SettingsItem(
                    icon = Icons.Filled.PictureAsPdf,
                    title = "Export to PDF",
                    subtitle = "Export your readings history",
                    onClick = { viewModel.exportData() }
                )
                SettingsItem(
                    icon = Icons.Filled.TableChart,
                    title = "Export to CSV",
                    subtitle = "Export for spreadsheet analysis",
                    onClick = { viewModel.exportCsv() }
                )
                SettingsItem(
                    icon = Icons.Filled.LocalHospital,
                    title = "Doctor Visit Report",
                    subtitle = "Generate a summary report for your doctor",
                    onClick = { viewModel.generateDoctorReport() }
                )
                SettingsItem(
                    icon = Icons.Filled.DeleteForever,
                    title = "Delete All Data",
                    subtitle = "Remove all readings permanently",
                    onClick = { showDeleteDialog = true },
                    isDestructive = true
                )
            }
            
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "About",
                    subtitle = "Version 1.0.0",
                    onClick = { showAboutDialog = true }
                )
                SettingsItem(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy",
                    onClick = { }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Blood Pressure Tracker v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error 
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
