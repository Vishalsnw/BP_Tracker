package com.bptracker.ui.screens.quickentry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bptracker.data.model.BloodPressureCategory
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.ui.components.getCategoryColor
import com.bptracker.ui.screens.addreading.AddReadingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEntryScreen(
    onNavigateBack: () -> Unit,
    onExpandToFull: () -> Unit,
    viewModel: AddReadingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var systolicText by remember { mutableStateOf("") }
    var diastolicText by remember { mutableStateOf("") }
    var pulseText by remember { mutableStateOf("") }
    
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }
    
    val systolic = systolicText.toIntOrNull() ?: 0
    val diastolic = diastolicText.toIntOrNull() ?: 0
    val pulse = pulseText.toIntOrNull() ?: 0
    
    val category = if (systolic > 0 && diastolic > 0) {
        BloodPressureReading(systolic = systolic, diastolic = diastolic, pulse = pulse).category
    } else null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Entry", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = onExpandToFull) {
                        Text("More Options")
                        Icon(Icons.Filled.ExpandMore, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Enter your reading",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "Just type the numbers from your BP monitor",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = systolicText,
                    onValueChange = { 
                        if (it.length <= 3 && it.all { c -> c.isDigit() }) {
                            systolicText = it
                            it.toIntOrNull()?.let { value -> 
                                viewModel.updateSystolic(value) 
                            }
                        }
                    },
                    label = { Text("SYS") },
                    placeholder = { Text("120") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(100.dp),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = "/",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = diastolicText,
                    onValueChange = { 
                        if (it.length <= 3 && it.all { c -> c.isDigit() }) {
                            diastolicText = it
                            it.toIntOrNull()?.let { value -> 
                                viewModel.updateDiastolic(value) 
                            }
                        }
                    },
                    label = { Text("DIA") },
                    placeholder = { Text("80") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(100.dp),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            OutlinedTextField(
                value = pulseText,
                onValueChange = { 
                    if (it.length <= 3 && it.all { c -> c.isDigit() }) {
                        pulseText = it
                        it.toIntOrNull()?.let { value -> 
                            viewModel.updatePulse(value) 
                        }
                    }
                },
                label = { Text("Pulse (optional)") },
                placeholder = { Text("72") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(150.dp),
                leadingIcon = {
                    Icon(Icons.Filled.Favorite, contentDescription = null)
                },
                suffix = { Text("bpm") }
            )
            
            if (category != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = getCategoryColor(category).copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = when (category) {
                                BloodPressureCategory.NORMAL -> Icons.Filled.CheckCircle
                                BloodPressureCategory.ELEVATED -> Icons.Filled.Info
                                BloodPressureCategory.HIGH_STAGE_1 -> Icons.Filled.Warning
                                BloodPressureCategory.HIGH_STAGE_2 -> Icons.Filled.Error
                                BloodPressureCategory.HYPERTENSIVE_CRISIS -> Icons.Filled.Emergency
                            },
                            contentDescription = null,
                            tint = getCategoryColor(category)
                        )
                        Column {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = getCategoryColor(category)
                            )
                            Text(
                                text = category.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            if (uiState.validationError != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column {
                            uiState.validationError?.systolicError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                            uiState.validationError?.diastolicError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                            uiState.validationError?.pulseError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                            uiState.validationError?.relationError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { viewModel.saveReading() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = systolic > 0 && diastolic > 0
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Reading", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
