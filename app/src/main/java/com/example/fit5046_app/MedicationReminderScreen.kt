package com.example.fit5046_app

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fit5046_app.model.*
import com.example.fit5046_app.service.MedicationReminderService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationReminderScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var reminders by remember { mutableStateOf<List<MedicationReminder>>(emptyList()) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var selectedReminder by remember { mutableStateOf<MedicationReminder?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load reminders when screen is first displayed
    LaunchedEffect(Unit) {
        loadRemindersFromStorage(context) { newReminders ->
            reminders = newReminders

            // Schedule alarms for active reminders
            newReminders.forEach { reminder ->
                if (reminder.isActive) {
                    MedicationReminderService.scheduleReminder(context, reminder)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Medication Reminders",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Reminder Button
        Button(
            onClick = { showAddReminderDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Medication Reminder")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reminders List
        if (reminders.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "No reminders",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No medication reminders set",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add a reminder to get notified when it's time to take your medication",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onEdit = { selectedReminder = reminder },
                        onToggle = { isActive ->
                            // Update reminder status
                            val updatedReminders = reminders.map { r ->
                                if (r.id == reminder.id) r.copy(isActive = isActive) else r
                            }
                            reminders = updatedReminders
                            saveRemindersToStorage(context, updatedReminders)

                            // Schedule or cancel alarm
                            if (isActive) {
                                MedicationReminderService.scheduleReminder(context, reminder.copy(isActive = isActive))
                            } else {
                                MedicationReminderService.cancelReminder(context, reminder.id)
                            }
                        },
                        onDelete = {
                            // Cancel alarm first
                            MedicationReminderService.cancelReminder(context, reminder.id)

                            // Delete reminder
                            val updatedReminders = reminders.filter { it.id != reminder.id }
                            reminders = updatedReminders
                            saveRemindersToStorage(context, updatedReminders)
                        }
                    )
                }
            }
        }
    }

    // Add/Edit Reminder Dialog
    if (showAddReminderDialog || selectedReminder != null) {
        AddReminderDialog(
            reminder = selectedReminder,
            onDismiss = {
                showAddReminderDialog = false
                selectedReminder = null
            },
            onSave = { reminder ->
                val currentSelectedReminder = selectedReminder
                if (currentSelectedReminder != null) {
                    // Cancel old alarm first
                    MedicationReminderService.cancelReminder(context, currentSelectedReminder.id)

                    // Update existing reminder
                    val updatedReminders = reminders.map { r ->
                        if (r.id == reminder.id) reminder else r
                    }
                    reminders = updatedReminders
                    saveRemindersToStorage(context, updatedReminders)

                    // Schedule new alarm if active
                    if (reminder.isActive) {
                        MedicationReminderService.scheduleReminder(context, reminder)
                    }
                } else {
                    // Add new reminder
                    val updatedReminders = reminders + reminder
                    reminders = updatedReminders
                    saveRemindersToStorage(context, updatedReminders)

                    // Schedule alarm if active
                    if (reminder.isActive) {
                        MedicationReminderService.scheduleReminder(context, reminder)
                    }
                }
                showAddReminderDialog = false
                selectedReminder = null
            }
        )
    }
}

@Composable
fun ReminderCard(
    reminder: MedicationReminder,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.medicationName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dosage: ${reminder.dosage}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%02d:%02d", reminder.hour, reminder.minute),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days of week
            Text(
                text = "Days: ${reminder.daysOfWeek.joinToString(", ") { it.name.take(3) }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Toggle switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Active")
                Switch(
                    checked = reminder.isActive,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}

@Composable
fun AddReminderDialog(
    reminder: MedicationReminder?,
    onDismiss: () -> Unit,
    onSave: (MedicationReminder) -> Unit
) {
    val context = LocalContext.current
    var medicationName by remember { mutableStateOf(reminder?.medicationName ?: "") }
    var dosage by remember { mutableStateOf(reminder?.dosage ?: "") }
    var selectedHour by remember { mutableStateOf(reminder?.hour ?: 8) }
    var selectedMinute by remember { mutableStateOf(reminder?.minute ?: 0) }
    var selectedDays by remember { mutableStateOf(reminder?.daysOfWeek ?: ReminderDayOfWeek.values().toList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (reminder != null) "Edit Reminder" else "Add Reminder") },
        text = {
            Column {
                OutlinedTextField(
                    value = medicationName,
                    onValueChange = { medicationName = it },
                    label = { Text("Medication Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g., 10 units, 1 tablet)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))


                // Time picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Time: ${String.format("%02d:%02d", selectedHour, selectedMinute)}")
                    TextButton(onClick = {
                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _, hour: Int, minute: Int ->
                                selectedHour = hour
                                selectedMinute = minute
                            },
                            selectedHour,
                            selectedMinute,
                            false // 24-hour format
                        )
                        timePickerDialog.show()
                    }) {
                        Text("Change")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days of week selection
                Text("Days of week:")
                LazyRow {
                    items(ReminderDayOfWeek.values()) { day ->
                        FilterChip(
                            selected = selectedDays.contains(day),
                            onClick = {
                                selectedDays = if (selectedDays.contains(day)) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
                            label = { Text(day.name.take(3)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newReminder = MedicationReminder(
                        id = reminder?.id ?: UUID.randomUUID().toString(),
                        medicationName = medicationName,
                        dosage = dosage,
                        reminderType = ReminderType.TABLET,
                        timeOfDay = TimeOfDay.MORNING,
                        hour = selectedHour,
                        minute = selectedMinute,
                        daysOfWeek = selectedDays,
                        isActive = true
                    )
                    onSave(newReminder)
                },
                enabled = medicationName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Simple persistence using SharedPreferences with JSON
private fun loadRemindersFromStorage(
    context: Context,
    onResult: (List<MedicationReminder>) -> Unit
) {
    try {
        val sharedPrefs = context.getSharedPreferences("medication_reminders", Context.MODE_PRIVATE)
        val remindersJson = sharedPrefs.getString("reminders", "[]")

        if (remindersJson.isNullOrEmpty() || remindersJson == "[]") {
            onResult(emptyList())
            return
        }

        val gson = Gson()
        val type = object : TypeToken<List<MedicationReminder>>() {}.type
        val reminders = gson.fromJson<List<MedicationReminder>>(remindersJson, type) ?: emptyList()
        onResult(reminders)
    } catch (e: Exception) {
        onResult(emptyList())
    }
}

private fun saveRemindersToStorage(
    context: Context,
    reminders: List<MedicationReminder>
) {
    try {
        val sharedPrefs = context.getSharedPreferences("medication_reminders", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        val gson = Gson()
        val remindersJson = gson.toJson(reminders)
        editor.putString("reminders", remindersJson)
        editor.apply()
    } catch (e: Exception) {
        // Handle error silently for now
    }
}
