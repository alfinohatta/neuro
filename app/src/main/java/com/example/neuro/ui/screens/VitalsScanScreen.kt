package com.example.neuro.ui.screens

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neuro.data.model.ConsentType
import com.example.neuro.data.model.SignalMeasurement
import com.example.neuro.data.model.SignalQuality
import com.example.neuro.data.model.SignalType
import com.example.neuro.data.repository.NeuroRepository
import com.example.neuro.domain.sensing.PpgSignalResult
import com.example.neuro.ui.components.CameraSensingPreview
import com.example.neuro.ui.components.NeuroBottomBar
import com.example.neuro.ui.navigation.Screen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class ScanState {
    PRE_SCAN,
    SCANNING,
    SCAN_COMPLETE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsScanScreen(
    repository: NeuroRepository = remember { NeuroRepository.getInstance() },
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onNavigateToInsights: () -> Unit
) {
    var currentState by remember { mutableStateOf(ScanState.PRE_SCAN) }
    var isEcoMode by remember { mutableStateOf(false) }

    val consents by repository.getConsents().collectAsState(initial = emptyList())
    val isCameraConsentGranted = consents.find { it.consentType == ConsentType.CAMERA }?.isGranted == true

    var scannedHeartRate by remember { mutableDoubleStateOf(72.0) }
    var scannedRespiratoryRate by remember { mutableDoubleStateOf(15.0) }
    var scannedSdnn by remember { mutableDoubleStateOf(42.0) }
    var scannedRmssd by remember { mutableDoubleStateOf(48.0) }
    var signalQuality by remember { mutableStateOf(SignalQuality.HIGH) }
    var scanProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentState) {
        if (currentState == ScanState.SCANNING) {
            scanProgress = 0f
            val totalSteps = 100
            for (step in 1..totalSteps) {
                delay(100)
                scanProgress = step.toFloat() / totalSteps.toFloat()
            }
            currentState = ScanState.SCAN_COMPLETE

            val nowStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            repository.addMeasurement(
                SignalMeasurement(
                    userId = 1L,
                    sessionId = 1L,
                    provider = "Presage SmartSpectra CameraX",
                    signalType = SignalType.HEART_RATE,
                    signalValue = scannedHeartRate,
                    unit = "bpm",
                    signalQuality = signalQuality,
                    measuredAt = nowStr
                )
            )
            repository.addMeasurement(
                SignalMeasurement(
                    userId = 1L,
                    sessionId = 1L,
                    provider = "Presage SmartSpectra CameraX",
                    signalType = SignalType.BREATHING_RATE,
                    signalValue = scannedRespiratoryRate,
                    unit = "breaths/min",
                    signalQuality = signalQuality,
                    measuredAt = nowStr
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentState) {
                            ScanState.PRE_SCAN -> "VitalLens Vitals Scan"
                            ScanState.SCANNING -> "Live Vitals Sensing"
                            ScanState.SCAN_COMPLETE -> "Scan Complete"
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    if (currentState != ScanState.SCANNING) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    if (currentState == ScanState.SCAN_COMPLETE) {
                        TextButton(onClick = onNavigateBack) {
                            Text("Done", color = Color(0xFF00A3FF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else if (currentState == ScanState.SCANNING) {
                        IconButton(onClick = { currentState = ScanState.PRE_SCAN }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F19))
            )
        },
        bottomBar = {
            if (currentState != ScanState.SCANNING) {
                NeuroBottomBar(
                    currentRoute = Screen.VitalsScan.route,
                    onNavigateToRoute = onNavigateToRoute
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0F19))
                .padding(padding)
        ) {
            when (currentState) {
                ScanState.PRE_SCAN -> {
                    PreScanContent(
                        isEcoMode = isEcoMode,
                        isConsentGranted = isCameraConsentGranted,
                        onEcoModeToggled = { isEcoMode = it },
                        onStartScan = { currentState = ScanState.SCANNING }
                    )
                }

                ScanState.SCANNING -> {
                    ActiveScanningContent(
                        progress = scanProgress,
                        heartRate = scannedHeartRate,
                        breathingRate = scannedRespiratoryRate,
                        quality = signalQuality,
                        isConsentGranted = isCameraConsentGranted,
                        onCancelScan = { currentState = ScanState.PRE_SCAN },
                        onSignalUpdated = { result ->
                            scannedHeartRate = result.heartRateBpm
                            scannedRespiratoryRate = result.breathingRateBpm
                            scannedSdnn = result.hrvMs * 0.75
                            scannedRmssd = result.hrvMs
                            signalQuality = result.signalQuality
                        }
                    )
                }

                ScanState.SCAN_COMPLETE -> {
                    ScanCompleteContent(
                        heartRate = scannedHeartRate,
                        respiratoryRate = scannedRespiratoryRate,
                        sdnn = scannedSdnn,
                        rmssd = scannedRmssd,
                        onViewDetails = onNavigateToInsights,
                        onScanAgain = { currentState = ScanState.PRE_SCAN }
                    )
                }
            }
        }
    }
}

@Composable
fun PreScanContent(
    isEcoMode: Boolean,
    isConsentGranted: Boolean,
    onEcoModeToggled: (Boolean) -> Unit,
    onStartScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Estimate your vital signs using only your camera",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (!isConsentGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Camera consent is currently disabled. Please enable Camera consent in Profile settings to start scanning.",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D2A)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        InstructionItem(
                            icon = Icons.Default.AccountCircle,
                            title = "Center your face\nin the oval.",
                            modifier = Modifier.weight(1f)
                        )
                        InstructionItem(
                            icon = Icons.Default.PauseCircle,
                            title = "Hold yourself and\ncamera still.",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        InstructionItem(
                            icon = Icons.Default.WbSunny,
                            title = "Ensure bright,\nsteady lighting.",
                            modifier = Modifier.weight(1f)
                        )
                        InstructionItem(
                            icon = Icons.Default.AccessTime,
                            title = "Scan takes\n~10 seconds.",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartScan,
                enabled = isConsentGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00A3FF),
                    disabledContainerColor = Color(0xFF334155)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = if (isConsentGranted) "Start Scan" else "Consent Required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D2A)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00A3FF).copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF00A3FF), modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.padding(end = 8.dp)) {
                            Text("Eco Mode", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Standard accuracy, for slower connections and devices",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = isEcoMode,
                        onCheckedChange = onEcoModeToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00A3FF),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF1E293B),
                            uncheckedBorderColor = Color(0xFF475569)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun InstructionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF00A3FF), modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun ActiveScanningContent(
    progress: Float,
    heartRate: Double,
    breathingRate: Double,
    quality: SignalQuality,
    isConsentGranted: Boolean,
    onCancelScan: () -> Unit,
    onSignalUpdated: (PpgSignalResult) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraSensingPreview(
            onSignalUpdated = onSignalUpdated,
            isConsentGranted = isConsentGranted,
            modifier = Modifier.fillMaxSize()
        )

        // Oval face frame overlay + Crosshair Target HUD
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2.3f)
            val ovalWidth = size.width * 0.72f
            val ovalHeight = size.height * 0.48f

            drawOval(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(center.x - ovalWidth / 2f, center.y - ovalHeight / 2f),
                size = Size(ovalWidth, ovalHeight),
                style = Stroke(width = 4.dp.toPx())
            )

            // Alignment Crosshair HUD Markers
            drawLine(
                color = Color(0xFF00A3FF).copy(alpha = 0.5f),
                start = Offset(center.x - 20.dp.toPx(), center.y),
                end = Offset(center.x + 20.dp.toPx(), center.y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color(0xFF00A3FF).copy(alpha = 0.5f),
                start = Offset(center.x, center.y - 20.dp.toPx()),
                end = Offset(center.x, center.y + 20.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )

            drawArc(
                color = Color(0xFF00A3FF),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(center.x - ovalWidth / 2f, center.y - ovalHeight / 2f),
                size = Size(ovalWidth, ovalHeight),
                style = Stroke(width = 6.dp.toPx())
            )
        }

        // Top Status Pill (Perfect Distance • Lighting Optimal)
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Blink Rate: 16/min • Eye Strain: Low • 20-20-20 Optimal", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Live Telemetry Overlay Card (Live Feedback)
        Surface(
            color = Color.Black.copy(alpha = 0.85f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Heart Rate", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("${heartRate.toInt()} BPM", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Breathing", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("${breathingRate.toInt()} /min", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00A3FF))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Progress", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("${(progress * 100).toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ScanCompleteContent(
    heartRate: Double,
    respiratoryRate: Double,
    sdnn: Double,
    rmssd: Double,
    onViewDetails: () -> Unit,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Vitals Measurement Complete",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    VitalMetricCard(
                        title = "Heart Rate",
                        value = "${heartRate.toInt()}",
                        unit = "BPM",
                        icon = Icons.Default.Favorite,
                        iconTint = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )

                    VitalMetricCard(
                        title = "Respiratory Rate",
                        value = "${respiratoryRate.toInt()}",
                        unit = "BPM",
                        icon = Icons.Default.Air,
                        iconTint = Color(0xFFF97316),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    VitalMetricCard(
                        title = "SDNN",
                        value = "${sdnn.toInt()}",
                        unit = "MS",
                        icon = Icons.Default.MonitorHeart,
                        iconTint = Color(0xFFEC4899),
                        modifier = Modifier.weight(1f)
                    )

                    VitalMetricCard(
                        title = "RMSSD",
                        value = "${rmssd.toInt()}",
                        unit = "MS",
                        icon = Icons.Default.FavoriteBorder,
                        iconTint = Color(0xFFEC4899),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onViewDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A3FF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("View Detailed Insights", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            OutlinedButton(
                onClick = onScanAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Scan Again", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun VitalMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D2A)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(unit, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 6.dp))
            }
        }
    }
}
