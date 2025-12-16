package com.bptracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bptracker.data.model.BloodPressureCategory
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.ui.theme.*

@Composable
fun BloodPressureCard(
    reading: BloodPressureReading,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Reading") },
            text = { Text("Are you sure you want to delete this reading?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete?.invoke()
                    }
                ) {
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
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(reading.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = getCategoryColor(reading.category),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${reading.systolic}/${reading.diastolic}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " mmHg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MonitorHeart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PulseColor
                    )
                    Text(
                        text = "${reading.pulse} bpm",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(category = reading.category)
                    
                    if (reading.tag.label != "None") {
                        AssistChip(
                            onClick = {},
                            label = { Text(reading.tag.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = reading.formattedDateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onToggleFavorite != null) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (reading.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (reading.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (onDelete != null) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(category: BloodPressureCategory) {
    Surface(
        color = getCategoryColor(category).copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = category.label,
            style = MaterialTheme.typography.labelSmall,
            color = getCategoryColor(category),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun getCategoryColor(category: BloodPressureCategory): Color {
    return when (category) {
        BloodPressureCategory.NORMAL -> BPNormal
        BloodPressureCategory.ELEVATED -> BPElevated
        BloodPressureCategory.HIGH_STAGE_1 -> BPHighStage1
        BloodPressureCategory.HIGH_STAGE_2 -> BPHighStage2
        BloodPressureCategory.HYPERTENSIVE_CRISIS -> BPCrisis
    }
}
