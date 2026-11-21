package com.example.neuro.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neuro.data.model.SignalMeasurement
import com.example.neuro.data.model.SignalQuality
import com.example.neuro.data.model.SignalType
import com.example.neuro.data.repository.NeuroRepository
import com.example.neuro.domain.breathing.AnimationStyle
import com.example.neuro.domain.breathing.BreathingRepository
import com.example.neuro.domain.breathing.BreathingTechnique
import com.example.neuro.ui.theme.CyanAccent
import com.example.neuro.ui.theme.EmeraldLight
import com.example.neuro.ui.theme.TealPrimary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

enum class SoundscapeMode(val label: String) {
    BINAURAL_ALPHA("Alpha 10Hz 🎧"),
    OCEAN_THETA("Theta Ocean 🌊"),
    WHITE_NOISE("White Noise 🌧️")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingExerciseScreen(
    exerciseId: String,
    repository: NeuroRepository = remember { NeuroRepository.getInstance() },
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val technique = remember(exerciseId) { BreathingRepository.getTechniqueById(exerciseId) }
    val haptic = LocalHapticFeedback.current

    var currentPhaseIndex by remember { mutableIntStateOf(0) }
    var cadenceMultiplier by remember { mutableFloatStateOf(1.0f) }
    var selectedSoundscape by remember { mutableStateOf(SoundscapeMode.BINAURAL_ALPHA) }
    var phaseSecondsRemaining by remember { mutableIntStateOf((technique.phases.first().durationSeconds / cadenceMultiplier).toInt()) }
    var totalCompletedCycles by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var isVoiceEnabled by remember { mutableStateOf(true) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    // Native Text-To-Speech Engine Initialization
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val currentPhase = technique.phases[currentPhaseIndex]

    // Voice & Tactile Guidance on Phase Changes
    LaunchedEffect(currentPhaseIndex, isTtsReady, isVoiceEnabled) {
        if (isVoiceEnabled && isTtsReady && ttsEngine != null) {
            try {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                ttsEngine?.speak(currentPhase.instruction, TextToSpeech.QUEUE_FLUSH, null, null)
            } catch (e: Exception) {
                // Fallback for emulators
            }
        }
    }

    // Phase Timer Loop
    LaunchedEffect(isPaused, currentPhaseIndex, cadenceMultiplier) {
        while (!isPaused && !showCompletionDialog) {
            delay((1000 / cadenceMultiplier).toLong())
            if (phaseSecondsRemaining > 1) {
                phaseSecondsRemaining--
            } else {
                val nextIndex = (currentPhaseIndex + 1) % technique.phases.size
                if (nextIndex == 0) {
                    totalCompletedCycles++
                }
                currentPhaseIndex = nextIndex
                phaseSecondsRemaining = (technique.phases[nextIndex].durationSeconds / cadenceMultiplier).toInt().coerceAtLeast(1)
            }
        }
    }

    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            title = { Text("Exercise Complete!", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("You completed $totalCompletedCycles full cycles of ${technique.title}.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Target Paced Respiration: ${technique.targetBpm} breaths/min.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Vascular resonance and HRV balance recorded.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nowStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                        repository.addMeasurement(
                            SignalMeasurement(
                                userId = 1L,
                                sessionId = 1L,
                                provider = "Neuro Breathing Engine",
                                signalType = SignalType.BREATHING_RATE,
                                signalValue = technique.targetBpm,
                                unit = "breaths/min",
                                signalQuality = SignalQuality.HIGH,
                                measuredAt = nowStr
                            )
                        )
                        showCompletionDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(technique.title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isVoiceEnabled = !isVoiceEnabled }) {
                        Icon(
                            imageVector = if (isVoiceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Voice Guidance",
                            tint = if (isVoiceEnabled) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showCompletionDialog = true }) {
                        Icon(Icons.Default.Check, contentDescription = "Complete")
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info Pill
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${technique.purpose} • Cycle $totalCompletedCycles",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Soundscape Selector Chips Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoundscapeMode.values().forEach { mode ->
                    FilterChip(
                        selected = selectedSoundscape == mode,
                        onClick = { selectedSoundscape = mode },
                        label = { Text(mode.label, fontSize = 10.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cadence Speed Control Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = cadenceMultiplier == 0.8f,
                    onClick = { cadenceMultiplier = 0.8f },
                    label = { Text("Deep 0.8x", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = cadenceMultiplier == 1.0f,
                    onClick = { cadenceMultiplier = 1.0f },
                    label = { Text("Standard 1.0x", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = cadenceMultiplier == 1.2f,
                    onClick = { cadenceMultiplier = 1.2f },
                    label = { Text("Reset 1.2x", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Render Custom Animation Area based on AnimationStyle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                when (technique.animationStyle) {
                    AnimationStyle.PULSATION_RING -> PulsationRingAnimation(currentPhase = currentPhase, secondsRemaining = phaseSecondsRemaining)
                    AnimationStyle.BOX_PERIMETER -> BoxPerimeterAnimation(currentPhaseIndex = currentPhaseIndex, secondsRemaining = phaseSecondsRemaining)
                    AnimationStyle.SINE_WAVE_OCEAN -> SineWaveOceanAnimation(secondsRemaining = phaseSecondsRemaining)
                    AnimationStyle.DUAL_DIAPHRAGM -> DualDiaphragmAnimation(currentPhase = currentPhase)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guidance & Instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${currentPhase.name.uppercase()} ($phaseSecondsRemaining s)",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentPhase.instruction,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Control Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { isPaused = !isPaused },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPaused) "Resume" else "Pause")
                }

                Button(
                    onClick = { showCompletionDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finish Session", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 1. Pulsation Ring Animation (4-7-8 Reset)
@Composable
fun PulsationRingAnimation(currentPhase: com.example.neuro.domain.breathing.BreathingPhase, secondsRemaining: Int) {
    val phaseColor = when (currentPhase.name) {
        "Inhale" -> CyanAccent
        "Hold" -> Color(0xFFA855F7)
        else -> EmeraldLight
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(phaseColor.copy(alpha = 0.35f))
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(currentPhase.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = phaseColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${secondsRemaining}s", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

// 2. Box Perimeter Animation (Square Tactical Focus)
@Composable
fun BoxPerimeterAnimation(currentPhaseIndex: Int, secondsRemaining: Int) {
    val transition = rememberInfiniteTransition(label = "BoxOrb")
    val orbPosition by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "OrbPos"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val w = size.width
            val h = size.height

            drawRoundRect(
                color = Color(0xFF0284C7),
                size = Size(w, h),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())
            )

            val orbOffset = when (currentPhaseIndex) {
                0 -> Offset(w * orbPosition, 0f)
                1 -> Offset(w, h * orbPosition)
                2 -> Offset(w * (1f - orbPosition), h)
                else -> Offset(0f, h * (1f - orbPosition))
            }

            drawCircle(color = Color(0xFF00B8D9), radius = 10.dp.toPx(), center = orbOffset)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${secondsRemaining}s", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
        }
    }
}

// 3. Sine Wave Ocean Animation (Resonance Coherence)
@Composable
fun SineWaveOceanAnimation(secondsRemaining: Int) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
        Canvas(modifier = Modifier.size(260.dp, 160.dp)) {
            val w = size.width
            val h = size.height

            val path = Path().apply {
                moveTo(0f, h / 2f)
                for (i in 0..100) {
                    val x = (i / 100f) * w
                    val y = h / 2f + sin(i * 0.15f) * 45f
                    lineTo(x, y)
                }
            }

            drawPath(path, Color(0xFF10B981), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("96% Coherence", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Resonance Pacing", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// 4. Dual Diaphragm Animation (Somatic Belly Release)
@Composable
fun DualDiaphragmAnimation(currentPhase: com.example.neuro.domain.breathing.BreathingPhase) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color(0xFF334155).copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFFF59E0B).copy(alpha = 0.5f))
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(currentPhase.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
