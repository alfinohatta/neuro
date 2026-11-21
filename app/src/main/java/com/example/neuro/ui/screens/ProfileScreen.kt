package com.example.neuro.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neuro.data.model.ConsentType
import com.example.neuro.data.repository.NeuroRepository
import com.example.neuro.ui.components.NeuroBottomBar
import com.example.neuro.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: NeuroRepository = remember { NeuroRepository.getInstance() },
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    val context = LocalContext.current
    val consents by repository.getConsents().collectAsState(initial = emptyList())

    var isHealthConnectSynced by remember { mutableStateOf(true) }
    var isUltraContrastEnabled by remember { mutableStateOf(false) }

    val isCameraGranted = consents.find { it.consentType == ConsentType.CAMERA }?.isGranted == true
    val isSignalGranted = consents.find { it.consentType == ConsentType.SIGNAL_PROCESSING }?.isGranted == true
    val isAnalyticsGranted = consents.find { it.consentType == ConsentType.ANALYTICS }?.isGranted == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NeuroBottomBar(
                currentRoute = Screen.Profile.route,
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
            // User Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF00B8D9),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("AH", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Alfino Hatta", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("alfino@example.com", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "CONSENT VERIFIED 🔒",
                                color = Color(0xFF059669),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Outdoor Ultra-Contrast Sunlight Theme Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.padding(end = 8.dp)) {
                            Text("Outdoor Ultra-Contrast Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("WCAG AAA high-glare sunlight readability", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isUltraContrastEnabled,
                        onCheckedChange = {
                            isUltraContrastEnabled = it
                            Toast.makeText(context, if (it) "Outdoor Ultra-Contrast enabled" else "Standard theme active", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Health Connect Integration Card
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
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.padding(end = 8.dp)) {
                                Text("Google Health Connect", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Bidirectional Sleep & HRV Sync", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isHealthConnectSynced,
                            onCheckedChange = {
                                isHealthConnectSynced = it
                                Toast.makeText(context, if (it) "Health Connect sync enabled" else "Health Connect sync disabled", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (isHealthConnectSynced) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Resting HR", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("62 bpm", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Baseline HRV", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("52 ms", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sleep Score", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("88%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }

            Text("Granular Privacy Consents", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            // 1. Camera Sensing Consent
            ConsentItemCard(
                title = "Camera Contactless Sensing",
                description = "Allows live in-memory Y-plane ppg extraction. Frames are disposed immediately without saving.",
                isGranted = isCameraGranted,
                onToggle = { repository.updateConsent(ConsentType.CAMERA, it) }
            )

            // 2. Signal Processing Consent
            ConsentItemCard(
                title = "Signal Processing & Telemetry",
                description = "Enables calculating heart rate, respiratory rates, and HRV coherence metrics during sessions.",
                isGranted = isSignalGranted,
                onToggle = { repository.updateConsent(ConsentType.SIGNAL_PROCESSING, it) }
            )

            // 3. Analytics Consent
            ConsentItemCard(
                title = "Productivity Analytics",
                description = "Allows saving session summaries to local database for personal progress tracking.",
                isGranted = isAnalyticsGranted,
                onToggle = { repository.updateConsent(ConsentType.ANALYTICS, it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Non-Medical Wellness Disclaimer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00B8D9))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Non-Medical Wellness Notice", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Neuro Focus observations are intended for personal wellness, focus optimization, and general fitness purposes only, and do not constitute medical diagnosis or treatment.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ConsentItemCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = isGranted,
                onCheckedChange = onToggle
            )
        }
    }
}
