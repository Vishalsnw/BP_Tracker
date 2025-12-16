package com.bptracker.ui.screens.history

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

enum class HistoryFilter(val label: String) {
    ALL("All"),
    FAVORITES("Favorites"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onEditReading: (Long) -> Unit,
    onAddReading: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(selectedFilter) {
        viewModel.setFilter(selectedFilter)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            HistoryFilter.entries.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter.label) },
                                    onClick = {
                                        selectedFilter = filter
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        if (selectedFilter == filter) {
                                            Icon(Icons.Filled.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.exportToPdf() }) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = "Export PDF")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReading) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        if (uiState.readings.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "No readings found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedFilter != HistoryFilter.ALL) {
                        TextButton(onClick = { selectedFilter = HistoryFilter.ALL }) {
                            Text("Clear filter")
                        }
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
                    FilterChip(
                        selected = selectedFilter != HistoryFilter.ALL,
                        onClick = { showFilterMenu = true },
                        label = { Text(selectedFilter.label) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                
                item {
                    Text(
                        text = "${uiState.readings.size} readings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                items(
                    items = uiState.readings,
                    key = { it.id }
                ) { reading ->
                    BloodPressureCard(
                        reading = reading,
                        onClick = { onEditReading(reading.id) },
                        onDelete = { viewModel.deleteReading(reading) },
                        onToggleFavorite = { viewModel.toggleFavorite(reading) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
