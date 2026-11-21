package com.example.neuro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neuro.data.model.ConsentType
import com.example.neuro.domain.calibration.BaselineCalibrationEngine
import com.example.neuro.domain.recommendation.CognitiveStrainEngine
import com.example.neuro.ui.components.NeuroBottomBar
import com.example.neuro.ui.navigation.Screen
import com.example.neuro.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAiChat by remember { mutableStateOf(false) }

    val cameraConsentGranted = state.consents.find { it.consentType == ConsentType.CAMERA }?.isGranted == true

    val strainEngine = remember { CognitiveStrainEngine() }
    val strainStatus = remember(state.deepWorkMinutes, state.heartRate, state.breathingRate, state.hrvValue) {
        strainEngine.calculateStrain(
            elapsedMinutes = 25,
            heartRateBpm = state.heartRate.toDouble(),
            breathingRateBpm = state.breathingRate.toDouble(),
            hrvMs = state.hrvValue.toDouble()
        )
    }

    val baselineProfile = remember(state.sessions.size, state.hrvValue) {
        BaselineCalibrationEngine.calculateBaseline(
            sessionsCount = state.sessions.size,
            averageHrv = state.hrvValue.toDouble()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NeuroOS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF00B8D9)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAiChat = !showAiChat }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF475569)
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color(0xFF475569)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NeuroBottomBar(
                currentRoute = Screen.Dashboard.route,
                onNavigateToRoute = onNavigateToRoute
            )
        },
        floatingActionButton = {
            Button(
                onClick = { showAiChat = !showAiChat },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Chat",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Chat",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // System & Consent Status Banner
                Surface(
                    color = if (cameraConsentGranted) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (cameraConsentGranted) Color(0xFF10B981) else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (cameraConsentGranted) "Presage Sensing Active • Explicit Consent Verified" else "Camera Consent Limited • Tap to Configure",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (cameraConsentGranted) Color(0xFF065F46) else Color(0xFF92400E)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = onNavigateToProfile,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }

                // Interruption Logger & Motion Telemetry Card
                InterruptionLoggerCard()

                // Adaptive Baseline Calibration Card
                BaselineCalibrationCard(
                    daysCalibrated = baselineProfile.daysCalibrated,
                    baselineHrv = baselineProfile.baselineHrvMs.toInt(),
                    baselineHr = baselineProfile.baselineRestingHrBpm.toInt(),
                    statusText = baselineProfile.calibrationStatusText
                )

                // Team Coherence & Group Sprint Card
                TeamCoherenceCard()

                // Cognitive Strain & Circadian Peak Card
                CognitiveStrainCard(strainScore = strainStatus.strainScore, recommendation = strainStatus.recommendationText)

                // Sleep & Recovery Biomarker Integration Card
                SleepRecoveryCard()

                // Focus Streak & Achievement Badges Card
                FocusStreakCard()

                // Top Row: Focus Gauge Widget & Habit Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FocusGaugeCard(
                        focusScore = state.focusScore,
                        deepWorkTime = "${state.deepWorkMinutes / 60}h ${state.deepWorkMinutes % 60}m",
                        flowState = "High",
                        modifier = Modifier.weight(1.25f)
                    )

                    HabitGridCard(
                        modifier = Modifier.weight(1f)
                    )
                }

                // 30-Day Focus Activity Heatmap Matrix Card
                FocusHeatmapCard()

                // Middle Row: Dual Biometric Trend Graph Card
                BiometricGraphCard(
                    hrvValue = state.hrvValue,
                    breathingRate = state.breathingRate
                )

                // Quick Vitals Strip
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Heart Rate", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("${state.heartRate} bpm", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Respiratory", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("${state.breathingRate} /min", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("HRV Index", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("${state.hrvValue} ms", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                        }
                        Button(
                            onClick = onNavigateToScan,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B8D9)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                // Recent Sessions & Summaries Stream
                Text("Recent Focus Sessions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))

                state.sessions.forEach { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(session.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                                Text(
                                    "${session.plannedDurationMinutes} mins • ${session.sessionType.name}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Surface(
                                color = if (session.status.name == "COMPLETED") Color(0xFFD1FAE5) else Color(0xFFDBEAFE),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = session.status.name,
                                    color = if (session.status.name == "COMPLETED") Color(0xFF059669) else Color(0xFF2563EB),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))
            }

            // Floating AI Chat Dialog Overlay
            AnimatedVisibility(
                visible = showAiChat,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 76.dp, end = 16.dp)
            ) {
                AiChatCard(
                    onDismiss = { showAiChat = false },
                    onActionClick = {
                        showAiChat = false
                        onNavigateToScan()
                    }
                )
            }
        }
    }
}

@Composable
fun InterruptionLoggerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Proactive Focus Shield", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                }
                Surface(
                    color = Color(0xFFDBEAFE),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("2 SWITCHES", color = Color(0xFF2563EB), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "2 app switches logged today • Focus Shield preventing context switching drop-offs.",
                fontSize = 12.sp,
                color = Color(0xFF475569)
            )
        }
    }
}

@Composable
fun BaselineCalibrationCard(
    daysCalibrated: Int,
    baselineHrv: Int,
    baselineHr: Int,
    statusText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adaptive Baseline Thresholds", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                }
                Surface(
                    color = Color(0xFFCCFBF1),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("$daysCalibrated / 7 DAYS", color = Color(0xFF0D9488), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = statusText,
                fontSize = 12.sp,
                color = Color(0xFF475569)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Baseline HRV", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("$baselineHrv ms", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Resting HR", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("$baselineHr bpm", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sensitivity", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("Adaptive", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
    }
}

@Composable
fun TeamCoherenceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Team Sprint Alpha", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                }
                Surface(
                    color = Color(0xFFE0F2FE),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("94% GROUP FLOW", color = Color(0xFF0284C7), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Alfino", "Ramina", "Thomas", "Sarah").forEach { name ->
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Collective strain optimal • Synchronized break prompt scheduled in 15 mins",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun SleepRecoveryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bedtime, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sleep & Recovery Integration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                }
                Surface(
                    color = Color(0xFFEEF2FF),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("92% RECOVERY", color = Color(0xFF6366F1), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Deep Sleep: 2h 15m • REM: 1h 45m • Optimal mental resilience for deep work today.",
                fontSize = 13.sp,
                color = Color(0xFF475569)
            )
        }
    }
}

@Composable
fun CognitiveStrainCard(
    strainScore: Int,
    recommendation: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cognitive Load & Peak Window", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                }
                Surface(
                    color = Color(0xFFCCFBF1),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("$strainScore / 100", color = Color(0xFF0D9488), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = recommendation,
                fontSize = 13.sp,
                color = Color(0xFF475569)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Peak Deep Work Window:", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                }
                Text("9:00 AM – 11:30 AM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            }
        }
    }
}

@Composable
fun FocusStreakCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("7-Day Deep Work Streak", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                }
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("STREAK ⚡", color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("7-Day Streak", Color(0xFF10B981)),
                    Pair("Vagal Master", Color(0xFF8B5CF6)),
                    Pair("50h Focus", Color(0xFF06B6D4))
                ).forEach { (badge, color) ->
                    Surface(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FocusHeatmapCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "30-Day Activity Heatmap Matrix",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val densityColors = listOf(
                Color(0xFF10B981), Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFF334155), Color(0xFF10B981), Color(0xFF06B6D4),
                Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFF10B981), Color(0xFF334155),
                Color(0xFF10B981), Color(0xFF334155), Color(0xFF10B981), Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFF10B981),
                Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFF10B981), Color(0xFF334155), Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF10B981)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                densityColors.chunked(7).forEach { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        week.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FocusGaugeCard(
    focusScore: Int,
    deepWorkTime: String,
    flowState: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Focus",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF334155)
                )
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = null,
                    tint = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(105.dp)
                ) {
                    Canvas(modifier = Modifier.size(95.dp)) {
                        val strokeWidthPx = 10.dp.toPx()

                        drawCircle(
                            color = Color(0xFFE2E8F0),
                            style = Stroke(width = strokeWidthPx)
                        )

                        val gradient = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF10B981),
                                Color(0xFF06B6D4),
                                Color(0xFF3B82F6),
                                Color(0xFFEC4899),
                                Color(0xFF10B981)
                            )
                        )

                        drawArc(
                            brush = gradient,
                            startAngle = -220f,
                            sweepAngle = 290f * (focusScore / 100f),
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }

                    Text(
                        text = "$focusScore%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Deep Work:",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = deepWorkTime,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Flow State:",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = flowState,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
fun HabitGridCard(modifier: Modifier = Modifier) {
    val days = listOf("M", "Tu", "W", "Th", "F")
    val dotColors = listOf(
        listOf(Color(0xFF10B981), Color(0xFF10B981), Color(0xFF10B981), Color(0xFF10B981), Color(0xFF10B981)),
        listOf(Color(0xFF10B981), Color(0xFF94A3B8), Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF94A3B8)),
        listOf(Color(0xFF94A3B8), Color(0xFF06B6D4), Color(0xFF06B6D4), Color(0xFF06B6D4), Color(0xFF94A3B8)),
        listOf(Color(0xFF94A3B8), Color(0xFF94A3B8), Color(0xFFF59E0B), Color(0xFFF59E0B), Color(0xFFF59E0B)),
        listOf(Color(0xFFF59E0B), Color(0xFF94A3B8), Color(0xFF94A3B8), Color(0xFF94A3B8), Color(0xFF94A3B8))
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Habit Grid",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                days.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            dotColors.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    row.forEach { dotColor ->
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricGraphCard(
    hrvValue: Int,
    breathingRate: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Biometric graph",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF334155)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "HRV $hrvValue",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Breathing $breathingRate/min",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val w = size.width
                val h = size.height

                val pathHrv = Path().apply {
                    moveTo(0f, h * 0.70f)
                    cubicTo(w * 0.20f, h * 0.35f, w * 0.35f, h * 0.30f, w * 0.50f, h * 0.60f)
                    cubicTo(w * 0.65f, h * 0.85f, w * 0.80f, h * 0.30f, w, h * 0.40f)
                }

                drawPath(
                    path = pathHrv,
                    color = Color(0xFF3B82F6),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                val pathBreathing = Path().apply {
                    moveTo(0f, h * 0.80f)
                    cubicTo(w * 0.25f, h * 0.70f, w * 0.45f, h * 0.65f, w * 0.60f, h * 0.50f)
                    cubicTo(w * 0.72f, h * 0.38f, w * 0.88f, h * 0.45f, w, h * 0.65f)
                }

                drawPath(
                    path = pathBreathing,
                    color = Color(0xFFEF4444),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
fun AiChatCard(
    onDismiss: () -> Unit,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(310.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = Color(0xFF9333EA),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Assistant",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your HRV is 71 ms, indicating optimal flow state. You've completed 4h 15m of deep work today!",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF475569),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Start Vital Camera Scan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
