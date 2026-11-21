package com.example.neuro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.sin

@Composable
fun PhysiologicalTelemetryView(
    modifier: Modifier = Modifier,
    heartRateBpm: Int = 76,
    upperBreathingRate: Int = 15,
    lowerBreathingRate: Int = 15,
    participantId: String = "Participant 014",
    isCameraConsentGranted: Boolean = true
) {
    var showPpgTrace by remember { mutableStateOf(true) }
    var showUpperRespTrace by remember { mutableStateOf(true) }
    var showLowerRespTrace by remember { mutableStateOf(true) }

    var selectedTimeSeconds by remember { mutableFloatStateOf(525f) }

    val currentTimeMins = (selectedTimeSeconds / 60).toInt()
    val currentTimeSecs = (selectedTimeSeconds % 60).toInt()
    val timeLabel = String.format(Locale.US, "%02d:%02d", currentTimeMins, currentTimeSecs)

    val currentHr = (heartRateBpm + sin(selectedTimeSeconds * 0.05f) * 3).toInt()
    val currentUpperResp = (upperBreathingRate + sin(selectedTimeSeconds * 0.02f) * 1).toInt()
    val currentLowerResp = (lowerBreathingRate + sin(selectedTimeSeconds * 0.02f) * 1).toInt()
    val currentHrv = (48 + sin(selectedTimeSeconds * 0.03f) * 5).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECF3F4)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Header Section: Participant metadata + Camera status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFDCE7E9),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isCameraConsentGranted) Icons.Default.Info else Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF2C3E50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = participantId,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isCameraConsentGranted) "Camera Offline • Explicit Consent" else "Camera Consent Revoked",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VideocamOff,
                            contentDescription = "Camera Offline",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Signal Isolation Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showPpgTrace,
                    onClick = { showPpgTrace = !showPpgTrace },
                    label = { Text("PPG Pulse", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFE4E6),
                        selectedLabelColor = Color(0xFFE11D48)
                    )
                )
                FilterChip(
                    selected = showUpperRespTrace,
                    onClick = { showUpperRespTrace = !showUpperRespTrace },
                    label = { Text("Upper Resp.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE0F2FE),
                        selectedLabelColor = Color(0xFF0284C7)
                    )
                )
                FilterChip(
                    selected = showLowerRespTrace,
                    onClick = { showLowerRespTrace = !showLowerRespTrace },
                    label = { Text("Lower Resp.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF1F5F9),
                        selectedLabelColor = Color(0xFF475569)
                    )
                )
            }

            // Live Telemetry Readout Strip
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Heart Rate", fontSize = 11.sp, color = Color(0xFFE11D48), fontWeight = FontWeight.SemiBold)
                        Text("$currentHr bpm", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Upper Resp.", fontSize = 11.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold)
                        Text("$currentUpperResp /min", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Lower Resp.", fontSize = 11.sp, color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                        Text("$currentLowerResp /min", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HRV", fontSize = 11.sp, color = Color(0xFF0D9488), fontWeight = FontWeight.SemiBold)
                        Text("$currentHrv ms", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                }
            }

            // Clinical Multi-Trace Canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .padding(12.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Trace 1: PPG Heart Rate Pulse (Top Red Trace)
                        if (showPpgTrace) {
                            val ppgPath = Path()
                            val ppgYOffset = h * 0.22f
                            ppgPath.moveTo(0f, ppgYOffset)

                            val step = w / 30f
                            for (i in 0..30) {
                                val x = i * step
                                val isPeak = i % 3 == 0
                                val y = if (isPeak) ppgYOffset - 22.dp.toPx() else ppgYOffset + (sin(i.toDouble()).toFloat() * 6.dp.toPx())
                                if (i == 0) ppgPath.moveTo(x, y) else ppgPath.lineTo(x, y)
                            }

                            drawPath(
                                path = ppgPath,
                                color = Color(0xFFE11D48),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Trace 2: Upper Respiration (Middle Dark Charcoal Trace)
                        if (showUpperRespTrace) {
                            val upperRespPath = Path()
                            val upperYOffset = h * 0.55f

                            val step = w / 40f
                            for (i in 0..40) {
                                val x = i * step
                                val y = upperYOffset + (sin(i * 0.25) * 16.dp.toPx()).toFloat()
                                if (i == 0) upperRespPath.moveTo(x, y) else upperRespPath.lineTo(x, y)
                            }

                            drawPath(
                                path = upperRespPath,
                                color = Color(0xFF1E293B),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Trace 3: Lower Respiration (Bottom Slate Gray Trace)
                        if (showLowerRespTrace) {
                            val lowerRespPath = Path()
                            val lowerYOffset = h * 0.82f

                            val step = w / 40f
                            for (i in 0..40) {
                                val x = i * step
                                val y = lowerYOffset + (sin(i * 0.20) * 14.dp.toPx()).toFloat()
                                if (i == 0) lowerRespPath.moveTo(x, y) else lowerRespPath.lineTo(x, y)
                            }

                            drawPath(
                                path = lowerRespPath,
                                color = Color(0xFF94A3B8),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Interactive Scrubber Line
                        val ScrubberX = (selectedTimeSeconds / 1500f) * w
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(ScrubberX, 0f),
                            end = Offset(ScrubberX, h),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = 5.dp.toPx(),
                            center = Offset(ScrubberX, 0f)
                        )
                    }

                    // Floating Event Marker 1: Sleep 3/5
                    Surface(
                        color = Color(0xFFE11D48),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.offset(x = 75.dp, y = 14.dp)
                    ) {
                        Text(
                            text = "Sleep • 3/5",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Floating Event Marker 2: Energy 4/5
                    Surface(
                        color = Color(0xFFE11D48),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.offset(x = 175.dp, y = 14.dp)
                    ) {
                        Text(
                            text = "Energy • 4/5",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Clinical Sympathovagal Spectrum Card (LF/HF Ratio)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sympathovagal Balance (LF/HF)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                        Surface(
                            color = Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("1.15 BALANCED", color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "LF (0.04-0.15Hz): 45% • HF (0.15-0.40Hz): 55% • Optimal parasympathetic modulation.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Scrubber Slider Control
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("00:00", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("Time: $timeLabel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("25:00", fontSize = 11.sp, color = Color(0xFF64748B))
                }

                Slider(
                    value = selectedTimeSeconds,
                    onValueChange = { selectedTimeSeconds = it },
                    valueRange = 0f..1500f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1E293B),
                        activeTrackColor = Color(0xFF1E293B)
                    )
                )
            }
        }
    }
}
