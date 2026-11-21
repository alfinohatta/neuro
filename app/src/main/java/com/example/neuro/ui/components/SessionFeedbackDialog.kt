package com.example.neuro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neuro.data.model.SessionFeedback
import com.example.neuro.ui.theme.EmeraldLight
import com.example.neuro.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionFeedbackDialog(
    sessionId: Long,
    onDismiss: () -> Unit,
    onSubmitFeedback: (SessionFeedback) -> Unit
) {
    var focusRating by remember { mutableIntStateOf(4) }
    var moodRating by remember { mutableIntStateOf(4) }
    var productivityRating by remember { mutableIntStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Session Completed!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "How was your working experience during this focus session?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Focus Rating Star Bar
                Column {
                    Text("Focus Rating", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    RatingBar(rating = focusRating, onRatingSelected = { focusRating = it })
                }

                // Mood Rating Star Bar
                Column {
                    Text("Mood Rating", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    RatingBar(rating = moodRating, onRatingSelected = { moodRating = it })
                }

                // Productivity Rating Star Bar
                Column {
                    Text("Productivity Rating", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    RatingBar(rating = productivityRating, onRatingSelected = { productivityRating = it })
                }

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Optional Notes / Reflection") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val feedback = SessionFeedback(
                        userId = 1L,
                        sessionId = sessionId,
                        focusRating = focusRating,
                        moodRating = moodRating,
                        productivityRating = productivityRating,
                        feedbackText = feedbackText.ifBlank { null }
                    )
                    onSubmitFeedback(feedback)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Save Summary & Exit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}

@Composable
fun RatingBar(
    rating: Int,
    onRatingSelected: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star $i",
                tint = if (i <= rating) Color(0xFFF59E0B) else Color.Gray,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onRatingSelected(i) }
            )
        }
    }
}
