package com.vitalflowapp.ui.screens.medication

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitalflowapp.data.model.Medication
import com.vitalflowapp.data.model.MedicationFrequency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<Medication?>(null) }
    
    if (showAddDialog || editingMedication != null) {
        AddMedicationDialog(
            medication = editingMedication,
            onDismiss = { 
                showAddDialog = false
                editingMedication = null
            },
            onSave = { medication ->
                if (editingMedication != null) {
                    viewModel.updateMedication(medication)
                } else {
                    viewModel.addMedication(medication)
                }
                showAddDialog = false
                editingMedication = null
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Medication")
            }
        }
    ) { padding ->
        if (uiState.medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Medication,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Medications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Track your blood pressure medications to see how they affect your readings over time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Medication")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Active Medications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(uiState.medications.filter { it.isActive }) { medication ->
                    MedicationCard(
                        medication = medication,
                        onEdit = { editingMedication = medication },
                        onToggleActive = { viewModel.toggleMedicationActive(medication) },
                        onDelete = { viewModel.deleteMedication(medication) }
                    )
                }
                
                val inactiveMeds = uiState.medications.filter { !it.isActive }
                if (inactiveMeds.isNotEmpty()) {
                    item {
                        Text(
                            text = "Inactive Medications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    items(inactiveMeds) { medication ->
                        MedicationCard(
                            medication = medication,
                            onEdit = { editingMedication = medication },
                            onToggleActive = { viewModel.toggleMedicationActive(medication) },
                            onDelete = { viewModel.deleteMedication(medication) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Medication") },
            text = { Text("Are you sure you want to delete ${medication.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (medication.isActive) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = medication.dosage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(medication.frequency.label) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                
                AssistChip(
                    onClick = { },
                    label = { Text("Since ${medication.formattedStartDate}") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (!medication.isActive) {
                    AssistChip(
                        onClick = onToggleActive,
                        label = { Text("Reactivate") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                } else {
                    AssistChip(
                        onClick = onToggleActive,
                        label = { Text("Stop") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
            
            if (medication.sideEffects.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Side Effects: ${medication.sideEffects}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
            
            if (medication.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = medication.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMedicationDialog(
    medication: Medication?,
    onDismiss: () -> Unit,
    onSave: (Medication) -> Unit
) {
    var name by remember { mutableStateOf(medication?.name ?: "") }
    var dosage by remember { mutableStateOf(medication?.dosage ?: "") }
    var frequency by remember { mutableStateOf(medication?.frequency ?: MedicationFrequency.DAILY) }
    var notes by remember { mutableStateOf(medication?.notes ?: "") }
    var sideEffects by remember { mutableStateOf(medication?.sideEffects ?: "") }
    var expandedFrequency by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (medication != null) "Edit Medication" else "Add Medication") 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g., 10mg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expandedFrequency,
                    onExpandedChange = { expandedFrequency = it }
                ) {
                    OutlinedTextField(
                        value = frequency.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { 
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrequency) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFrequency,
                        onDismissRequest = { expandedFrequency = false }
                    ) {
                        MedicationFrequency.entries.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.label) },
                                onClick = {
                                    frequency = freq
                                    expandedFrequency = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = sideEffects,
                    onValueChange = { sideEffects = it },
                    label = { Text("Side Effects (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && dosage.isNotBlank()) {
                        onSave(
                            Medication(
                                id = medication?.id ?: 0,
                                name = name,
                                dosage = dosage,
                                frequency = frequency,
                                notes = notes,
                                sideEffects = sideEffects,
                                isActive = medication?.isActive ?: true,
                                userId = medication?.userId ?: 0,
                                startDate = medication?.startDate ?: System.currentTimeMillis(),
                                endDate = medication?.endDate
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && dosage.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
