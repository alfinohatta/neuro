package com.example.neuro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neuro.ui.components.CameraSensingPreview
import com.example.neuro.ui.components.PpgWaveformView
import com.example.neuro.ui.components.SessionFeedbackDialog
import com.example.neuro.ui.theme.*
import com.example.neuro.ui.viewmodel.FocusSessionViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    viewModel: FocusSessionViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showMicroBreakPrompt by remember { mutableStateOf(false) }

    val totalPlannedSeconds = (state.currentSession?.plannedDurationMinutes ?: 25) * 60
    val remainingSeconds = (totalPlannedSeconds - state.elapsedTimeSeconds).coerceAtLeast(0)
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    var waveHistory by remember { mutableStateOf(listOf<Float>()) }

    // Coherence-Driven Dynamic Theme Shading for Biofeedback Aura
    val auraColor = when {
        state.currentHrv > 60 -> EmeraldLight.copy(alpha = 0.25f)
        state.currentHrv > 40 -> CyanAccent.copy(alpha = 0.20f)
        else -> Color(0xFFF59E0B).copy(alpha = 0.25f)
    }

    // Rhythmic Visual Aura Pulsation
    val infiniteTransition = rememberInfiniteTransition(label = "SessionAura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraScale"
    )

    if (showFeedbackDialog) {
        SessionFeedbackDialog(
            sessionId = state.currentSession?.id ?: 1L,
            onDismiss = {
                showFeedbackDialog = false
                onNavigateBack()
            },
            onSubmitFeedback = { feedback ->
                viewModel.submitFeedback(feedback)
                showFeedbackDialog = false
                onNavigateBack()
            }
        )
    }

    if (showMicroBreakPrompt) {
        AlertDialog(
            onDismissRequest = { showMicroBreakPrompt = false },
            title = { Text("Biometric Fatigue Detected", fontWeight = FontWeight.Bold) },
            text = { Text("Your HRV has decreased by 25% over the last 15 minutes. Take a 3-minute 4-7-8 parasympathetic reset break now?") },
            confirmButton = {
                Button(
                    onClick = {
                        showMicroBreakPrompt = false
                        viewModel.togglePause()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Start 3-Min Break")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMicroBreakPrompt = false }) {
                    Text("Continue Focusing")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.currentSession?.title ?: "Focus Session", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.finishSession()
                        showFeedbackDialog = true
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { showMicroBreakPrompt = true }) {
                        Icon(Icons.Default.SelfImprovement, contentDescription = "Micro-Break Prompt", tint = TealPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Sensing Active Status Badge
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldLight)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Presage SmartSpectra: ${state.signalQuality.name} Quality",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Real CameraX Contactless Sensing Preview
            CameraSensingPreview(
                onSignalUpdated = { ppgResult ->
                    viewModel.updateRealPpgSignal(ppgResult)
                    waveHistory = (waveHistory + ppgResult.normalizedSignalValue).takeLast(40)
                }
            )

            // Live Optical Pulse Waveform Canvas
            if (waveHistory.isNotEmpty()) {
                PpgWaveformView(waveHistory = waveHistory)
            }

            // Central Timer Circle Display with Pulsating Biofeedback Aura Glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                // Rhythmic Aura Glow Outer Ring (Dynamic Coherence Shading)
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .scale(auraScale)
                        .clip(CircleShape)
                        .background(auraColor)
                )

                // Main Inner Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .border(4.dp, TealPrimary, CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeFormatted,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.isPaused) "PAUSED" else "FOCUSING",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.isPaused) HeartRateRed else CyanAccent
                        )
                    }
                }
            }

            // Soundscape Audio Pacer & Sonic Coherence Synthesizer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = HrvPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Sonic Coherence Synthesizer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Harmonic Pitch: 432Hz • Resonant Swell: Active 🎼", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Surface(
                        color = HrvPurple.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ACTIVE 🎧", color = HrvPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }

            // Real-Time Human Signals Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Heart Rate Metric
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = HeartRateRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Heart Rate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "${state.currentHeartRate.toInt()} bpm",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Breathing Rate Metric
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Air, contentDescription = null, tint = BreathingBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Breathing", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "${state.currentBreathingRate.toInt()} /min",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // HRV Metric
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = HrvPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HRV", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "${state.currentHrv.toInt()} ms",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Adaptive Recommendation Alert Banner
            AnimatedVisibility(visible = state.activeRecommendation != null) {
                state.activeRecommendation?.let { recommendation ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = TealPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(recommendation.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(recommendation.message, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { viewModel.dismissRecommendation() }) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }
            }

            // Play / Pause & Finish Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.togglePause() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (state.isPaused) "Resume" else "Pause")
                }

                Button(
                    onClick = {
                        viewModel.finishSession()
                        showFeedbackDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finish", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
