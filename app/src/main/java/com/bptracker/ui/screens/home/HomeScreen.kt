package com.bptracker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bptracker.ui.components.BloodPressureCard
import com.bptracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddReading: () -> Unit,
    onViewHistory: () -> Unit,
    onEditReading: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Blood Pressure",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddReading,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add Reading") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SummaryCard(
                    totalReadings = uiState.totalReadings,
                    lastReading = uiState.recentReadings.firstOrNull()
                )
            }
            
            if (uiState.recentReadings.isNotEmpty()) {
                item {
                    QuickStatsCard(
                        avgSystolic = uiState.avgSystolic,
                        avgDiastolic = uiState.avgDiastolic,
                        avgPulse = uiState.avgPulse
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Readings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onViewHistory) {
                            Text("View All")
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                items(
                    items = uiState.recentReadings.take(5),
                    key = { it.id }
                ) { reading ->
                    BloodPressureCard(
                        reading = reading,
                        onClick = { onEditReading(reading.id) },
                        onDelete = { viewModel.deleteReading(reading) },
                        onToggleFavorite = { viewModel.toggleFavorite(reading) }
                    )
                }
            } else {
                item {
                    EmptyStateCard(onAddReading = onAddReading)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalReadings: Int,
    lastReading: com.bptracker.data.model.BloodPressureReading?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Last Reading",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    if (lastReading != null) {
                        Text(
                            text = "${lastReading.systolic}/${lastReading.diastolic}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = lastReading.formattedDateTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = "No readings yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = totalReadings.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "readings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsCard(
    avgSystolic: Double,
    avgDiastolic: Double,
    avgPulse: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "7-Day Averages",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Systolic",
                    value = if (avgSystolic > 0) "%.0f".format(avgSystolic) else "-",
                    color = SystolicColor
                )
                StatItem(
                    label = "Diastolic",
                    value = if (avgDiastolic > 0) "%.0f".format(avgDiastolic) else "-",
                    color = DiastolicColor
                )
                StatItem(
                    label = "Pulse",
                    value = if (avgPulse > 0) "%.0f".format(avgPulse) else "-",
                    color = PulseColor
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyStateCard(onAddReading: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.MonitorHeart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Start Tracking",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Add your first blood pressure reading to start monitoring your heart health.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onAddReading) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add First Reading")
            }
        }
    }
}
