package com.bptracker.ui.screens.addreading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.bptracker.data.model.*
import com.bptracker.ui.components.CategoryChip
import com.bptracker.ui.components.NumberPicker
import com.bptracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReadingScreen(
    readingId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddReadingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(readingId) {
        if (readingId != null && readingId > 0) {
            viewModel.loadReading(readingId)
        }
    }
    
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (readingId != null && readingId > 0) "Edit Reading" else "Add Reading",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveReading() },
                        enabled = !uiState.isLoading
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Blood Pressure",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberPicker(
                            value = uiState.systolic,
                            onValueChange = { viewModel.updateSystolic(it) },
                            range = 60..250,
                            label = "Systolic",
                            color = SystolicColor
                        )
                        
                        Text(
                            text = "/",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        NumberPicker(
                            value = uiState.diastolic,
                            onValueChange = { viewModel.updateDiastolic(it) },
                            range = 40..150,
                            label = "Diastolic",
                            color = DiastolicColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "mmHg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonitorHeart,
                            contentDescription = null,
                            tint = PulseColor
                        )
                        Text(
                            text = "Pulse Rate",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    NumberPicker(
                        value = uiState.pulse,
                        onValueChange = { viewModel.updatePulse(it) },
                        range = 40..200,
                        label = "BPM",
                        color = PulseColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val category = BloodPressureReading(
                        systolic = uiState.systolic,
                        diastolic = uiState.diastolic,
                        pulse = uiState.pulse
                    ).category
                    
                    CategoryChip(category = category)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            var expandedTag by remember { mutableStateOf(false) }
            var expandedArm by remember { mutableStateOf(false) }
            var expandedBody by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expandedTag,
                onExpandedChange = { expandedTag = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.tag.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tag / Time of Day") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTag) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedTag,
                    onDismissRequest = { expandedTag = false }
                ) {
                    ReadingTag.entries.forEach { tag ->
                        DropdownMenuItem(
                            text = { Text(tag.label) },
                            onClick = {
                                viewModel.updateTag(tag)
                                expandedTag = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedArm,
                    onExpandedChange = { expandedArm = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.armPosition.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Arm") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedArm) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedArm,
                        onDismissRequest = { expandedArm = false }
                    ) {
                        ArmPosition.entries.forEach { arm ->
                            DropdownMenuItem(
                                text = { Text(arm.label) },
                                onClick = {
                                    viewModel.updateArmPosition(arm)
                                    expandedArm = false
                                }
                            )
                        }
                    }
                }
                
                ExposedDropdownMenuBox(
                    expanded = expandedBody,
                    onExpandedChange = { expandedBody = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.bodyPosition.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Position") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBody) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedBody,
                        onDismissRequest = { expandedBody = false }
                    ) {
                        BodyPosition.entries.forEach { body ->
                            DropdownMenuItem(
                                text = { Text(body.label) },
                                onClick = {
                                    viewModel.updateBodyPosition(body)
                                    expandedBody = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
