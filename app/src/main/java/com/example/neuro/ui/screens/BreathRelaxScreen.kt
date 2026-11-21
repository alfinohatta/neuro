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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neuro.domain.breathing.BreathingRepository
import com.example.neuro.domain.breathing.BreathingTechnique
import com.example.neuro.ui.components.NeuroBottomBar
import com.example.neuro.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathRelaxScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onStartBreathingExercise: (exerciseId: String) -> Unit
) {
    val techniques = remember { BreathingRepository.techniques }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breath & Relax", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToRoute(Screen.Insights.route) }) {
                        Icon(Icons.Default.Info, contentDescription = "Insights Info")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NeuroBottomBar(
                currentRoute = Screen.BreathRelax.route,
                onNavigateToRoute = onNavigateToRoute
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Sensor Telemetry Summary Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Presage SmartSpectra", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                            }
                            Text("Connected • Camera Sensing Active", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Heart Rate Metric Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Heart Rate", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("82 BPM", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // BR Card (Purple Gradient)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Air, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("BR", fontSize = 11.sp, color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("19.6 B/min", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        // HRV Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("HRV", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("26.45 ms", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            Text("Guided Breathing & Relaxation Techniques", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            // Render 4 Unique Breathing Technique Cards
            techniques.forEach { technique ->
                BreathingTechniqueCard(
                    technique = technique,
                    onStart = { onStartBreathingExercise(technique.id) }
                )
            }
        }
    }
}

@Composable
fun BreathingTechniqueCard(
    technique: BreathingTechnique,
    onStart: () -> Unit
) {
    val cardGradient = when (technique.id) {
        "478_reset" -> listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
        "box_focus" -> listOf(Color(0xFF06B6D4), Color(0xFF0284C7))
        "resonance_hrv" -> listOf(Color(0xFF10B981), Color(0xFF059669))
        else -> listOf(Color(0xFF334155), Color(0xFF1E293B))
    }

    Surface(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(cardGradient))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${technique.defaultDurationMinutes} min • ${technique.instructor}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    IconButton(onClick = onStart) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = technique.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = technique.purpose,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = onStart,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Benefit Tags & Phase Breakdown Pill
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    technique.benefitTags.forEach { tag ->
                        Surface(
                            color = Color.Black.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tag,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
