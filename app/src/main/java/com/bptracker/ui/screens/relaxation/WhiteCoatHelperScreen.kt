package com.bptracker.ui.screens.relaxation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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

enum class RelaxationPhase {
    INTRO,
    SIT_COMFORTABLY,
    RELAX_SHOULDERS,
    DEEP_BREATHS,
    PREPARE,
    READY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteCoatHelperScreen(
    onNavigateBack: () -> Unit,
    onStartMeasurement: () -> Unit
) {
    var currentPhase by remember { mutableStateOf(RelaxationPhase.INTRO) }
    var secondsRemaining by remember { mutableStateOf(0) }
    var isActive by remember { mutableStateOf(false) }
    var totalSeconds by remember { mutableStateOf(0) }
    
    val phaseDurations = mapOf(
        RelaxationPhase.INTRO to 5,
        RelaxationPhase.SIT_COMFORTABLY to 15,
        RelaxationPhase.RELAX_SHOULDERS to 15,
        RelaxationPhase.DEEP_BREATHS to 30,
        RelaxationPhase.PREPARE to 10,
        RelaxationPhase.READY to 0
    )
    
    LaunchedEffect(isActive) {
        if (isActive) {
            val phases = listOf(
                RelaxationPhase.INTRO,
                RelaxationPhase.SIT_COMFORTABLY,
                RelaxationPhase.RELAX_SHOULDERS,
                RelaxationPhase.DEEP_BREATHS,
                RelaxationPhase.PREPARE,
                RelaxationPhase.READY
            )
            
            for (phase in phases) {
                if (!isActive) break
                currentPhase = phase
                val duration = phaseDurations[phase] ?: 0
                
                for (i in duration downTo 0) {
                    if (!isActive) break
                    secondsRemaining = i
                    if (i > 0) delay(1000)
                }
            }
        }
    }
    
    val phaseProgress by animateFloatAsState(
        targetValue = when (currentPhase) {
            RelaxationPhase.INTRO -> 0.1f
            RelaxationPhase.SIT_COMFORTABLY -> 0.3f
            RelaxationPhase.RELAX_SHOULDERS -> 0.5f
            RelaxationPhase.DEEP_BREATHS -> 0.7f
            RelaxationPhase.PREPARE -> 0.9f
            RelaxationPhase.READY -> 1f
        },
        animationSpec = tween(500),
        label = "progress"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pre-Measurement Relaxation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        isActive = false
                        onNavigateBack()
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isActive && currentPhase == RelaxationPhase.INTRO) {
                IntroContent(
                    onStart = { isActive = true }
                )
            } else if (currentPhase == RelaxationPhase.READY) {
                ReadyContent(
                    onMeasure = onStartMeasurement
                )
            } else {
                RelaxationContent(
                    phase = currentPhase,
                    secondsRemaining = secondsRemaining,
                    progress = phaseProgress,
                    onStop = { 
                        isActive = false
                        currentPhase = RelaxationPhase.INTRO
                    }
                )
            }
        }
    }
}

@Composable
private fun IntroContent(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            Icons.Filled.SelfImprovement,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "White Coat Syndrome Helper",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Feeling anxious before measuring your blood pressure? This guided relaxation will help you calm down for a more accurate reading.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "What to expect:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "Sit comfortably for 15 seconds",
                    "Relax your shoulders and arms",
                    "Take slow, deep breaths",
                    "Prepare for measurement"
                ).forEach { step ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Begin Relaxation", fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "About 1 minute",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RelaxationContent(
    phase: RelaxationPhase,
    secondsRemaining: Int,
    progress: Float,
    onStop: () -> Unit
) {
    val phaseInfo = when (phase) {
        RelaxationPhase.INTRO -> Pair("Getting Started", "Find a comfortable position...")
        RelaxationPhase.SIT_COMFORTABLY -> Pair("Sit Comfortably", "Rest your back against the chair.\nKeep your feet flat on the floor.\nPlace your arm on a flat surface.")
        RelaxationPhase.RELAX_SHOULDERS -> Pair("Relax Your Body", "Drop your shoulders.\nUnclench your jaw.\nRelax your hands.")
        RelaxationPhase.DEEP_BREATHS -> Pair("Deep Breaths", "Breathe in slowly through your nose...\nHold for a moment...\nExhale slowly through your mouth.")
        RelaxationPhase.PREPARE -> Pair("Almost Ready", "Keep breathing calmly.\nPrepare your blood pressure monitor.")
        RelaxationPhase.READY -> Pair("Ready!", "You're ready to measure.")
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            Canvas(modifier = Modifier.size(200.dp * breathingScale)) {
                drawCircle(
                    color = Color(0xFF6750A4).copy(alpha = 0.2f)
                )
                drawCircle(
                    color = Color(0xFF6750A4),
                    style = Stroke(width = 4f)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$secondsRemaining",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "seconds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = phaseInfo.first,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = phaseInfo.second,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        TextButton(onClick = onStop) {
            Icon(Icons.Filled.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop")
        }
    }
}

@Composable
private fun ReadyContent(onMeasure: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "You're Ready!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You should be feeling calmer now.\nGo ahead and take your measurement.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onMeasure,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Reading Now", fontWeight = FontWeight.SemiBold)
        }
    }
}
