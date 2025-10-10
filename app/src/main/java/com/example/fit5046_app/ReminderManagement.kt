package com.example.fit5046_app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReminderManagement(
    onNavigateBack: () -> Unit = {}
) {
    // Reminder Settings State
    var medicationReminders by remember { mutableStateOf(true) }
    var glucoseCheckReminders by remember { mutableStateOf(true) }
    var exerciseReminders by remember { mutableStateOf(false) }
    var mealReminders by remember { mutableStateOf(true) }

    // Time settings
    var medicationTime by remember { mutableStateOf("08:00") }
    var glucoseCheckTime by remember { mutableStateOf("09:00") }
    var exerciseTime by remember { mutableStateOf("18:00") }
    var mealTime by remember { mutableStateOf("12:00") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Reminder Management",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Medication Reminders
        ReminderCard(
            title = "Medication Reminders",
            subtitle = "Remind me to take my medications",
            icon = Icons.Default.Medication,
            checked = medicationReminders,
            onCheckedChange = { medicationReminders = it },
            time = medicationTime,
            onTimeChange = { medicationTime = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Glucose Check Reminders
        ReminderCard(
            title = "Glucose Check Reminders",
            subtitle = "Remind me to check blood glucose levels",
            icon = Icons.Default.MonitorHeart,
            checked = glucoseCheckReminders,
            onCheckedChange = { glucoseCheckReminders = it },
            time = glucoseCheckTime,
            onTimeChange = { glucoseCheckTime = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Exercise Reminders
        ReminderCard(
            title = "Exercise Reminders",
            subtitle = "Remind me to exercise",
            icon = Icons.Default.FitnessCenter,
            checked = exerciseReminders,
            onCheckedChange = { exerciseReminders = it },
            time = exerciseTime,
            onTimeChange = { exerciseTime = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Meal Reminders
        ReminderCard(
            title = "Meal Reminders",
            subtitle = "Remind me about meal times",
            icon = Icons.Default.Restaurant,
            checked = mealReminders,
            onCheckedChange = { mealReminders = it },
            time = mealTime,
            onTimeChange = { mealTime = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cancel Button
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Cancel",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel")
            }

            // Save Button
            Button(
                onClick = {
                    /* TODO: Save reminder settings */
                    onNavigateBack() // Navigate back after saving
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save")
            }
        }
    }
}

@Composable
private fun ReminderCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    time: String,
    onTimeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }

            if (checked) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reminder Time:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = time,
                        onValueChange = onTimeChange,
                        label = { Text("HH:MM") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                    )
                }
            }
        }
    }
}
