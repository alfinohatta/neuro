package com.example.neuro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neuro.data.model.ConsentType
import com.example.neuro.data.repository.NeuroRepository
import com.example.neuro.domain.export.TelemetryReportExporter
import com.example.neuro.ui.components.NeuroBottomBar
import com.example.neuro.ui.components.PhysiologicalTelemetryView
import com.example.neuro.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    repository: NeuroRepository = remember { NeuroRepository.getInstance() },
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    val context = LocalContext.current
    val sessions by repository.getSessions().collectAsState(initial = emptyList())
    val consents by repository.getConsents().collectAsState(initial = emptyList())
    val measurements by repository.getMeasurements().collectAsState(initial = emptyList())

    val isCameraConsentGranted = consents.find { it.consentType == ConsentType.CAMERA }?.isGranted == true

    val totalFocusMins = sessions.filter { it.status.name == "COMPLETED" }.sumOf { it.plannedDurationMinutes }
    val hrMeasurements = measurements.filter { it.signalType == com.example.neuro.data.model.SignalType.HEART_RATE }
    val avgHrBpm = if (hrMeasurements.isNotEmpty()) hrMeasurements.map { it.signalValue }.average().toInt() else 76
    val brMeasurements = measurements.filter { it.signalType == com.example.neuro.data.model.SignalType.BREATHING_RATE }
    val avgBr = if (brMeasurements.isNotEmpty()) brMeasurements.map { it.signalValue }.average().toInt() else 15

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Personal Focus Insights",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        TelemetryReportExporter.exportTelemetryCsv(context, totalFocusMins, avgHrBpm)
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export Telemetry", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        bottomBar = {
            NeuroBottomBar(
                currentRoute = Screen.Insights.route,
                onNavigateToRoute = onNavigateToRoute
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Session Summary Overview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Overview Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Total Focus Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Total Focus",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalFocusMins mins",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF0D9488)
                            )
                        }
                    }

                    // Avg Heart Rate Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Avg Heart Rate",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$avgHrBpm bpm",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }

            // Clinical Telemetry Multi-Trace View
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Physiological Telemetry Studio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PhysiologicalTelemetryView(
                    heartRateBpm = avgHrBpm,
                    upperBreathingRate = avgBr,
                    lowerBreathingRate = avgBr,
                    participantId = "Participant 014",
                    isCameraConsentGranted = isCameraConsentGranted
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Focus Sessions Log",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Session List Items
            items(sessions) { session ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${session.plannedDurationMinutes} mins planned • ${session.sessionType.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Surface(
                            color = when (session.status.name) {
                                "COMPLETED" -> Color(0xFFD1FAE5)
                                "ACTIVE" -> Color(0xFFDBEAFE)
                                else -> Color(0xFFE2E8F0)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = session.status.name,
                                color = when (session.status.name) {
                                    "COMPLETED" -> Color(0xFF059669)
                                    "ACTIVE" -> Color(0xFF2563EB)
                                    else -> Color(0xFF64748B)
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
