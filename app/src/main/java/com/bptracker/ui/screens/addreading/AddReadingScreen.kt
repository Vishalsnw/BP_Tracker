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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How are you feeling?",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MoodButton(emoji = "1", label = "Very Stressed", selected = uiState.mood == 1) {
                            viewModel.updateMood(1)
                        }
                        MoodButton(emoji = "2", label = "Stressed", selected = uiState.mood == 2) {
                            viewModel.updateMood(2)
                        }
                        MoodButton(emoji = "3", label = "Neutral", selected = uiState.mood == 3) {
                            viewModel.updateMood(3)
                        }
                        MoodButton(emoji = "4", label = "Happy", selected = uiState.mood == 4) {
                            viewModel.updateMood(4)
                        }
                        MoodButton(emoji = "5", label = "Very Happy", selected = uiState.mood == 5) {
                            viewModel.updateMood(5)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Stress Level",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("None" to 1, "Low" to 2, "Moderate" to 3, "High" to 4, "Severe" to 5).forEach { (label, value) ->
                            FilterChip(
                                selected = uiState.stressLevel == value,
                                onClick = { viewModel.updateStressLevel(value) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            if (uiState.validationError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Please check your readings",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.validationError?.systolicError?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        uiState.validationError?.diastolicError?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        uiState.validationError?.pulseError?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        uiState.validationError?.relationError?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodButton(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val moodEmoji = when(emoji) {
        "1" -> "X"
        "2" -> "-"
        "3" -> "O"
        "4" -> "+"
        "5" -> "*"
        else -> "O"
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { 
                Text(
                    text = moodEmoji,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
