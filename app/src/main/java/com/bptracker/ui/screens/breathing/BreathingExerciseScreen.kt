package com.bptracker.ui.screens.breathing

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class BreathingExercise(
    val name: String,
    val description: String,
    val inhaleSeconds: Int,
    val holdSeconds: Int,
    val exhaleSeconds: Int,
    val cycles: Int
)

val breathingExercises = listOf(
    BreathingExercise(
        name = "4-7-8 Relaxation",
        description = "A calming technique that helps reduce anxiety and lower blood pressure",
        inhaleSeconds = 4,
        holdSeconds = 7,
        exhaleSeconds = 8,
        cycles = 4
    ),
    BreathingExercise(
        name = "Box Breathing",
        description = "Used by Navy SEALs to stay calm under pressure",
        inhaleSeconds = 4,
        holdSeconds = 4,
        exhaleSeconds = 4,
        cycles = 6
    ),
    BreathingExercise(
        name = "Deep Belly Breathing",
        description = "Simple deep breathing to activate relaxation response",
        inhaleSeconds = 5,
        holdSeconds = 2,
        exhaleSeconds = 5,
        cycles = 8
    ),
    BreathingExercise(
        name = "Quick Calm",
        description = "A quick exercise for instant stress relief",
        inhaleSeconds = 3,
        holdSeconds = 3,
        exhaleSeconds = 6,
        cycles = 3
    )
)

enum class BreathingPhase {
    IDLE, INHALE, HOLD, EXHALE, COMPLETE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingExerciseScreen(
    onNavigateBack: () -> Unit
) {
    var selectedExercise by remember { mutableStateOf<BreathingExercise?>(null) }
    var isExercising by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf(BreathingPhase.IDLE) }
    var currentCycle by remember { mutableStateOf(1) }
    var secondsRemaining by remember { mutableStateOf(0) }
    
    LaunchedEffect(isExercising, selectedExercise) {
        if (isExercising && selectedExercise != null) {
            val exercise = selectedExercise!!
            currentCycle = 1
            
            while (currentCycle <= exercise.cycles && isExercising) {
                currentPhase = BreathingPhase.INHALE
                for (i in exercise.inhaleSeconds downTo 1) {
                    if (!isExercising) break
                    secondsRemaining = i
                    delay(1000)
                }
                
                if (!isExercising) break
                
                currentPhase = BreathingPhase.HOLD
                for (i in exercise.holdSeconds downTo 1) {
                    if (!isExercising) break
                    secondsRemaining = i
                    delay(1000)
                }
                
                if (!isExercising) break
                
                currentPhase = BreathingPhase.EXHALE
                for (i in exercise.exhaleSeconds downTo 1) {
                    if (!isExercising) break
                    secondsRemaining = i
                    delay(1000)
                }
                
                currentCycle++
            }
            
            if (isExercising) {
                currentPhase = BreathingPhase.COMPLETE
                delay(2000)
            }
            
            isExercising = false
            currentPhase = BreathingPhase.IDLE
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breathing Exercises", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        isExercising = false
                        onNavigateBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isExercising && selectedExercise != null) {
            BreathingExercisePlayer(
                exercise = selectedExercise!!,
                phase = currentPhase,
                currentCycle = currentCycle,
                secondsRemaining = secondsRemaining,
                onStop = { isExercising = false },
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Breathing exercises can help reduce stress and lower blood pressure naturally.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                breathingExercises.forEach { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onStart = {
                            selectedExercise = exercise
                            isExercising = true
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Regular practice of breathing exercises can help reduce blood pressure by 5-10 mmHg over time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: BreathingExercise,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimingChip("In ${exercise.inhaleSeconds}s")
                    TimingChip("Hold ${exercise.holdSeconds}s")
                    TimingChip("Out ${exercise.exhaleSeconds}s")
                }
                
                FilledTonalButton(onClick = onStart) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${exercise.cycles} cycles - ${(exercise.inhaleSeconds + exercise.holdSeconds + exercise.exhaleSeconds) * exercise.cycles / 60} min",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimingChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BreathingExercisePlayer(
    exercise: BreathingExercise,
    phase: BreathingPhase,
    currentCycle: Int,
    secondsRemaining: Int,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = when (phase) {
            BreathingPhase.INHALE -> 1f
            BreathingPhase.HOLD -> 1f
            BreathingPhase.EXHALE -> 0.6f
            else -> 0.6f
        },
        animationSpec = tween(
            durationMillis = when (phase) {
                BreathingPhase.INHALE -> exercise.inhaleSeconds * 1000
                BreathingPhase.EXHALE -> exercise.exhaleSeconds * 1000
                else -> 300
            },
            easing = LinearEasing
        ),
        label = "breathingScale"
    )
    
    val phaseColor = when (phase) {
        BreathingPhase.INHALE -> MaterialTheme.colorScheme.primary
        BreathingPhase.HOLD -> MaterialTheme.colorScheme.tertiary
        BreathingPhase.EXHALE -> MaterialTheme.colorScheme.secondary
        BreathingPhase.COMPLETE -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Cycle $currentCycle of ${exercise.cycles}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(250.dp)
        ) {
            Canvas(
                modifier = Modifier.size(250.dp * animatedScale)
            ) {
                drawCircle(
                    color = phaseColor.copy(alpha = 0.3f)
                )
                drawCircle(
                    color = phaseColor,
                    style = Stroke(width = 8f)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (phase) {
                        BreathingPhase.INHALE -> "Breathe In"
                        BreathingPhase.HOLD -> "Hold"
                        BreathingPhase.EXHALE -> "Breathe Out"
                        BreathingPhase.COMPLETE -> "Complete!"
                        else -> "Get Ready"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = phaseColor,
                    textAlign = TextAlign.Center
                )
                
                if (phase != BreathingPhase.COMPLETE && phase != BreathingPhase.IDLE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$secondsRemaining",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = phaseColor
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        FilledTonalButton(
            onClick = onStop,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Exercise")
        }
    }
}
