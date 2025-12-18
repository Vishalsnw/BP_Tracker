package com.bptracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bptracker.ui.theme.*

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
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete All Data", fontWeight = FontWeight.SemiBold) },
            text = { 
                Text("Are you sure you want to delete all readings? This action cannot be undone.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { 
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GradientHealthStart, GradientHealthEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MonitorHeart, 
                        contentDescription = null, 
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = { Text("BP Tracker", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Version 1.0.0",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
                Button(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            SettingsGroup(title = "Profiles") {
                SettingsItem(
                    icon = Icons.Filled.People,
                    iconColor = HealthcareIndigo,
                    title = "Family Profiles",
                    subtitle = "Manage profiles for family members",
                    onClick = onNavigateToProfiles
                )
            }
            
            SettingsGroup(title = "Health Tracking") {
                SettingsItem(
                    icon = Icons.Filled.MonitorWeight,
                    iconColor = AccentPurple,
                    title = "Weight Tracking",
                    subtitle = "Track weight and BMI",
                    onClick = onNavigateToWeight
                )
                SettingsItem(
                    icon = Icons.Filled.Bloodtype,
                    iconColor = AccentPink,
                    title = "Blood Glucose",
                    subtitle = "Monitor blood sugar levels",
                    onClick = onNavigateToGlucose,
                    showDivider = true
                )
                SettingsItem(
                    icon = Icons.Filled.Medication,
                    iconColor = HealthcareGreen,
                    title = "Medications",
                    subtitle = "Track your medications",
                    onClick = onNavigateToMedications,
                    showDivider = true
                )
                SettingsItem(
                    icon = Icons.Filled.Air,
                    iconColor = HealthcareTeal,
                    title = "Breathing Exercises",
                    subtitle = "Guided relaxation techniques",
                    onClick = onNavigateToBreathing
                )
            }
            
            SettingsGroup(title = "Goals & Insights") {
                SettingsItem(
                    icon = Icons.Filled.Flag,
                    iconColor = GoldStar,
                    title = "Health Goals",
                    subtitle = "Set and track your health targets",
                    onClick = onNavigateToGoals
                )
                SettingsItem(
                    icon = Icons.Filled.Insights,
                    iconColor = GradientBlueStart,
                    title = "Insights & Analytics",
                    subtitle = "Personalized health insights",
                    onClick = onNavigateToInsights
                )
            }
            
            SettingsGroup(title = "Devices & Sync") {
                SettingsItem(
                    icon = Icons.Filled.Bluetooth,
                    iconColor = DiastolicColor,
                    title = "Bluetooth BP Monitor",
                    subtitle = "Connect to Bluetooth monitors",
                    onClick = onNavigateToBluetooth
                )
                SettingsItem(
                    icon = Icons.Filled.HealthAndSafety,
                    iconColor = HealthcareGreen,
                    title = "Health Connect",
                    subtitle = "Sync with Android Health",
                    onClick = onNavigateToHealthConnect
                )
            }
            
            SettingsGroup(title = "Safety") {
                SettingsItem(
                    icon = Icons.Filled.Emergency,
                    iconColor = HealthcareRed,
                    title = "Crisis Response",
                    subtitle = "Emergency contacts and alerts",
                    onClick = onNavigateToEmergency
                )
            }
            
            SettingsGroup(title = "Notifications") {
                SettingsItem(
                    icon = Icons.Filled.Alarm,
                    iconColor = GradientSunsetStart,
                    title = "Measurement Reminders",
                    subtitle = "Set up daily reminders",
                    onClick = onNavigateToReminders
                )
            }
            
            SettingsGroup(title = "Data Management") {
                SettingsItem(
                    icon = Icons.Filled.PictureAsPdf,
                    iconColor = HealthcareRed,
                    title = "Export to PDF",
                    subtitle = "Export your readings history",
                    onClick = { viewModel.exportData() }
                )
                SettingsItem(
                    icon = Icons.Filled.TableChart,
                    iconColor = HealthcareGreen,
                    title = "Export to CSV",
                    subtitle = "Export for spreadsheet analysis",
                    onClick = { viewModel.exportCsv() },
                    showDivider = true
                )
                SettingsItem(
                    icon = Icons.Filled.LocalHospital,
                    iconColor = DiastolicColor,
                    title = "Doctor Visit Report",
                    subtitle = "Generate summary for your doctor",
                    onClick = { viewModel.generateDoctorReport() },
                    showDivider = true
                )
                SettingsItem(
                    icon = Icons.Filled.DeleteForever,
                    iconColor = HealthcareRed,
                    title = "Delete All Data",
                    subtitle = "Remove all readings permanently",
                    onClick = { showDeleteDialog = true },
                    isDestructive = true
                )
            }
            
            SettingsGroup(title = "About") {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    iconColor = HealthcareBlue,
                    title = "About",
                    subtitle = "Version 1.0.0",
                    onClick = { showAboutDialog = true }
                )
                SettingsItem(
                    icon = Icons.Filled.PrivacyTip,
                    iconColor = HealthcareIndigo,
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy",
                    onClick = { }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "BP Tracker v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    showDivider: Boolean = false
) {
    Column {
        if (showDivider) {
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDestructive) 
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        else 
                            iconColor.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) MaterialTheme.colorScheme.error else iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
