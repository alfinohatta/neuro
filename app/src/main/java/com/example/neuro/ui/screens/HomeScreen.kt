package com.example.neuro.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neuro.domain.gamification.CognitiveLevelEngine
import com.example.neuro.ui.components.NeuroBottomBar
import com.example.neuro.ui.navigation.Screen
import com.example.neuro.ui.theme.CyanAccent
import com.example.neuro.ui.theme.EmeraldLight
import com.example.neuro.ui.theme.HrvPurple
import com.example.neuro.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: com.example.neuro.data.repository.NeuroRepository = remember { com.example.neuro.data.repository.NeuroRepository.getInstance() },
    onStartSession: (title: String, duration: Int, type: String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var sessionTitle by remember { mutableStateOf("Deep Work Session") }
    var durationMinutes by remember { mutableFloatStateOf(25f) }
    var selectedType by remember { mutableStateOf("focus") }

    val sessions by repository.getSessions().collectAsState(initial = emptyList())
    val preferences by repository.getPreferences().collectAsState(initial = com.example.neuro.data.model.UserPreferences())
    val completedSessions = sessions.filter { it.status == com.example.neuro.data.model.SessionStatus.COMPLETED }
    val totalFocusMins = completedSessions.sumOf { it.plannedDurationMinutes }

    val levelStatus = remember(totalFocusMins, completedSessions.size) {
        CognitiveLevelEngine.calculateLevel(
            totalFocusMinutes = if (totalFocusMins > 0) totalFocusMins else 70,
            completedSessionsCount = completedSessions.size
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Neuro Focus", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Welcome back, Alfino", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NeuroBottomBar(
                currentRoute = Screen.Home.route,
                onNavigateToRoute = onNavigateToRoute
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cognitive Level & XP Milestone Card
            CognitiveLevelCard(
                levelNumber = levelStatus.levelNumber,
                levelTitle = levelStatus.levelTitle,
                totalXp = levelStatus.totalXp,
                progressPercent = levelStatus.progressPercent
            )

            // Weekly Focus Goal Challenge Card
            FocusChallengeCard()

            // Circadian Sleep-Focus Chronotype Profiler Card
            ChronotypeProfilerCard()

            // Weekly Insights Recap Card
            WeeklyFocusRecapCard()

            // Quick Camera Vitals Scan Banner
            Card(
                onClick = { onNavigateToRoute(Screen.VitalsScan.route) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyanAccent.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("VitalLens Vitals Scan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CyanAccent)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Estimate Heart Rate, Respiratory Rate, SDNN & RMSSD using only your camera.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onNavigateToRoute(Screen.VitalsScan.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Scan", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Quick Breathing & Relaxation Banner Card
            Card(
                onClick = { onNavigateToRoute(Screen.BreathRelax.route) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HrvPurple.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp)
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
                            color = HrvPurple,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Breath & Relax", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HrvPurple)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Controlled 4-7-8 breathing exercises to lower stress & restore flow state.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onNavigateToRoute(Screen.BreathRelax.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = HrvPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Daily Focus Goal Progress Card
            val dailyGoal = preferences.dailyFocusGoalMinutes
            val goalProgress = (totalFocusMins.toFloat() / dailyGoal.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Daily Focus Goal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("$totalFocusMins / $dailyGoal mins completed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${(goalProgress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = EmeraldLight, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { goalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = EmeraldLight,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // Quick Start Focus Session Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Start Focus Session", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = sessionTitle,
                        onValueChange = { sessionTitle = it },
                        label = { Text("Session Task / Goal") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Planned Duration: ${durationMinutes.toInt()} minutes", fontWeight = FontWeight.Medium)
                    Slider(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it },
                        valueRange = 5f..90f,
                        steps = 16,
                        colors = SliderDefaults.colors(thumbColor = TealPrimary, activeTrackColor = TealPrimary)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Session Mode", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == "focus",
                            onClick = { selectedType = "focus" },
                            label = { Text("Standard Focus") },
                            leadingIcon = { Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        FilterChip(
                            selected = selectedType == "deep_work",
                            onClick = { selectedType = "deep_work" },
                            label = { Text("Deep Work") },
                            leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onStartSession(sessionTitle, durationMinutes.toInt(), selectedType) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Begin Session with Sensing", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Recent Session Highlights
            Text("Recent Sessions", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            ListItem(
                headlineContent = { Text("Morning Deep Work") },
                supportingContent = { Text("25 mins • Avg Heart Rate: 76 bpm") },
                leadingContent = {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldLight)
                },
                trailingContent = { Text("85 Focus Score", fontWeight = FontWeight.Bold, color = CyanAccent) }
            )
        }
    }
}

@Composable
fun FocusChallengeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Weekly Coherence Sprint", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("2 / 3 SPRINTS", color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Complete 1 more Deep Work Sprint to earn the 'Vagal Resonance Master' badge!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChronotypeProfilerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bedtime, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Circadian Chronotype Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Surface(
                    color = TealPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("EARLY LARK 🌅", color = TealPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Peak Focus Range: 9:00 AM – 11:30 AM • Recommended Wind-down: 10:15 PM",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CognitiveLevelCard(
    levelNumber: Int,
    levelTitle: String,
    totalXp: Int,
    progressPercent: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = TealPrimary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("L$levelNumber", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(levelTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("$totalXp Cognitive XP Earned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    color = TealPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("LEVEL $levelNumber", color = TealPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = TealPrimary,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Room Ambient Sensing Indicator Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Room Environment:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Lux: 450 Optimal • Noise: 32 dB Quiet", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
            }
        }
    }
}

@Composable
fun WeeklyFocusRecapCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Weekly Focus Recap", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
                Surface(
                    color = Color(0xFF06B6D4).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("LAST 7 DAYS", color = Color(0xFF06B6D4), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Focus", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("8.5 hrs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Top Mode", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("Deep Work", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Avg Heart Rate", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("74 bpm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }
        }
    }
}
