package com.bptracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { 
                Text(
                    "Delete Reading",
                    fontWeight = FontWeight.SemiBold
                ) 
            },
            text = { 
                Text("Are you sure you want to delete this reading? This action cannot be undone.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                getCategoryColor(reading.category),
                                getCategoryColor(reading.category).copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    getCategoryColor(reading.category).copy(alpha = 0.15f),
                                    getCategoryColor(reading.category).copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = getCategoryColor(reading.category).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = getCategoryColor(reading.category),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = reading.category.label.take(6),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = getCategoryColor(reading.category),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${reading.systolic}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            ),
                            color = SystolicColor
                        )
                        Text(
                            text = "/",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        Text(
                            text = "${reading.diastolic}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            ),
                            color = DiastolicColor
                        )
                        Text(
                            text = " mmHg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(PulseColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MonitorHeart,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = PulseColor
                                )
                            }
                            Text(
                                text = "${reading.pulse} bpm",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = PulseColor
                            )
                        }
                        
                        if (reading.tag.label != "None") {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = reading.tag.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = reading.formattedDateTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onToggleFavorite != null) {
                        val starColor by animateColorAsState(
                            targetValue = if (reading.isFavorite) GoldStar else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            label = "starColor"
                        )
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (reading.isFavorite) GoldStar.copy(alpha = 0.1f) 
                                    else Color.Transparent
                                )
                        ) {
                            Icon(
                                imageVector = if (reading.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Toggle favorite",
                                tint = starColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    if (onDelete != null) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(category: BloodPressureCategory) {
    Surface(
        color = getCategoryColor(category).copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = getCategoryColor(category).copy(alpha = 0.3f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(category))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelMedium,
                color = getCategoryColor(category),
                fontWeight = FontWeight.SemiBold
            )
        }
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
