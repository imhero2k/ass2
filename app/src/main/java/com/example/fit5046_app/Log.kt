@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fit5046_app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fit5046_app.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Log() {
    var expandedDiet by remember { mutableStateOf(false) }
    var expandedBloodGlucose by remember { mutableStateOf(false) }
    var expandedExercise by remember { mutableStateOf(false) }
    var expandedMedication by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Add New Log Entry",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Diet Log Section
        ExpandableLogSection(
            title = "Diet Log",
            icon = Icons.Default.Add,
            isExpanded = expandedDiet,
            onToggle = { expandedDiet = !expandedDiet }
        ) {
            DietLogForm()
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Blood Glucose Log Section
        ExpandableLogSection(
            title = "Blood Glucose Log",
            icon = Icons.Default.Add,
            isExpanded = expandedBloodGlucose,
            onToggle = { expandedBloodGlucose = !expandedBloodGlucose }
        ) {
            BloodGlucoseLogForm()
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Exercise Log Section
        ExpandableLogSection(
            title = "Exercise Log",
            icon = Icons.Default.Add,
            isExpanded = expandedExercise,
            onToggle = { expandedExercise = !expandedExercise }
        ) {
            ExerciseLogForm()
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Medication Log Section
        ExpandableLogSection(
            title = "Medication Log",
            icon = Icons.Default.Add,
            isExpanded = expandedMedication,
            onToggle = { expandedMedication = !expandedMedication }
        ) {
            MedicationLogForm()
        }
    }
}

@Composable
fun ExpandableLogSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "rotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header with title and expand/collapse button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.rotate(rotationAngle)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expandable content
            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    time: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute,
        is24Hour = true
    )

    TimePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onTimeSelected(selectedTime)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        TimePicker(state = timePickerState)
    }
}

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Date") },
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Time") },
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@Composable
fun DietLogForm() {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var period by remember { mutableStateOf(EntryPeriod.BREAKFAST) }
    var notes by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var newFoodName by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Date and Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = date.format(dateFormatter),
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("📅")
                    }
                }
            )
            OutlinedTextField(
                value = time.format(timeFormatter),
                onValueChange = { },
                label = { Text("Time") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showTimePicker = true }) {
                        Text("🕐")
                    }
                }
            )
        }

        // Period
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = period.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Period") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                EntryPeriod.values().forEach { entryPeriod ->
                    DropdownMenuItem(
                        text = { Text(entryPeriod.displayName) },
                        onClick = {
                            period = entryPeriod
                            expanded = false
                        }
                    )
                }
            }
        }

        // Nutritional Information
        Text(
            text = "Nutritional Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                label = { Text("Calories *") },
                modifier = Modifier.weight(1f),
                isError = calories.isBlank()
            )
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it },
                label = { Text("Protein (g) *") },
                modifier = Modifier.weight(1f),
                isError = protein.isBlank()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it },
                label = { Text("Fat (g) *") },
                modifier = Modifier.weight(1f),
                isError = fat.isBlank()
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = it },
                label = { Text("Carbs (g) *") },
                modifier = Modifier.weight(1f),
                isError = carbs.isBlank()
            )
        }

        // Food Items
        OutlinedTextField(
            value = newFoodName,
            onValueChange = { newFoodName = it },
            label = { Text("Food Items") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Enter food items separated by commas (e.g., Apple, Banana, Rice)") },
            minLines = 2
        )

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // Save Button
        Button(
            onClick = {
                // Validate and save diet log
                val caloriesInt = calories.toIntOrNull()
                val proteinFloat = protein.toFloatOrNull()
                val fatFloat = fat.toFloatOrNull()
                val carbsFloat = carbs.toFloatOrNull()

                if (caloriesInt != null && proteinFloat != null && fatFloat != null && carbsFloat != null) {
                    // Parse comma-separated food items
                    val parsedFoodItems = if (newFoodName.isNotBlank()) {
                        newFoodName.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .map { FoodItemModel(it, null) }
                    } else {
                        emptyList()
                    }

                    val dietLog = DietLog(
                        id = "diet_${System.currentTimeMillis()}",
                        dateTime = LocalDateTime.of(date, time),
                        period = period,
                        notes = notes.ifBlank { null },
                        calories = caloriesInt,
                        proteinGrams = proteinFloat,
                        fatGrams = fatFloat,
                        carbGrams = carbsFloat,
                        foodItems = parsedFoodItems
                    )

                    // Here you would save to your data store
                    // For now, just show a success message and reset
                    notes = ""
                    calories = ""
                    protein = ""
                    fat = ""
                    carbs = ""
                    newFoodName = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = calories.isNotBlank() && protein.isNotBlank() && fat.isNotBlank() && carbs.isNotBlank()
        ) {
            Text("Save Diet Log")
        }
    }

    // Date and Time Picker Dialogs
    if (showDatePicker) {
        DatePickerDialog(
            date = date,
            onDateSelected = { selectedDate ->
                date = selectedDate
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            time = time,
            onTimeSelected = { selectedTime ->
                time = selectedTime
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun BloodGlucoseLogForm() {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var period by remember { mutableStateOf(EntryPeriod.BEFORE_BREAKFAST) }
    var notes by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Date and Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = date.format(dateFormatter),
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("📅")
                    }
                }
            )
            OutlinedTextField(
                value = time.format(timeFormatter),
                onValueChange = { },
                label = { Text("Time") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showTimePicker = true }) {
                        Text("🕐")
                    }
                }
            )
        }

        // Period
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = period.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Period") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                EntryPeriod.values().forEach { entryPeriod ->
                    DropdownMenuItem(
                        text = { Text(entryPeriod.displayName) },
                        onClick = {
                            period = entryPeriod
                            expanded = false
                        }
                    )
                }
            }
        }

        // Blood Glucose Value
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("Blood Glucose Level (mmol/L) *") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Normal range: 3.9 - 7.8 mmol/L") },
            isError = value.isBlank()
        )

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // Save Button
        Button(
            onClick = {
                // Validate and save blood glucose log
                val glucoseValue = value.toFloatOrNull()

                if (glucoseValue != null && glucoseValue > 0) {
                    val bloodGlucoseLog = BloodGlucoseLog(
                        id = "bg_${System.currentTimeMillis()}",
                        dateTime = LocalDateTime.of(date, time),
                        period = period,
                        notes = notes.ifBlank { null },
                        value = glucoseValue
                    )

                    // Here you would save to your data store
                    // For now, just show a success message and reset
                    notes = ""
                    value = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = value.isNotBlank() && value.toFloatOrNull() != null
        ) {
            Text("Save Blood Glucose Log")
        }
    }

    // Date and Time Picker Dialogs
    if (showDatePicker) {
        DatePickerDialog(
            date = date,
            onDateSelected = { selectedDate ->
                date = selectedDate
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            time = time,
            onTimeSelected = { selectedTime ->
                time = selectedTime
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun ExerciseLogForm() {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var period by remember { mutableStateOf(EntryPeriod.MORNING) }
    var notes by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var exerciseType by remember { mutableStateOf(ExerciseType.RUNNING) }
    var intensity by remember { mutableStateOf(Intensity.MEDIUM) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Date and Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = date.format(dateFormatter),
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("📅")
                    }
                }
            )
            OutlinedTextField(
                value = time.format(timeFormatter),
                onValueChange = { },
                label = { Text("Time") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showTimePicker = true }) {
                        Text("🕐")
                    }
                }
            )
        }

        // Period
        var periodExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = periodExpanded,
            onExpandedChange = { periodExpanded = !periodExpanded }
        ) {
            OutlinedTextField(
                value = period.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Period") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = periodExpanded,
                onDismissRequest = { periodExpanded = false }
            ) {
                EntryPeriod.values().forEach { entryPeriod ->
                    DropdownMenuItem(
                        text = { Text(entryPeriod.displayName) },
                        onClick = {
                            period = entryPeriod
                            periodExpanded = false
                        }
                    )
                }
            }
        }

        // Exercise Type
        var typeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded }
        ) {
            OutlinedTextField(
                value = exerciseType.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Exercise Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                ExerciseType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = {
                            exerciseType = type
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        // Duration and Intensity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration (minutes) *") },
                modifier = Modifier.weight(1f),
                isError = duration.isBlank()
            )

            var intensityExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = intensityExpanded,
                onExpandedChange = { intensityExpanded = !intensityExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = intensity.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Intensity") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intensityExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = intensityExpanded,
                    onDismissRequest = { intensityExpanded = false }
                ) {
                    Intensity.values().forEach { intensityLevel ->
                        DropdownMenuItem(
                            text = { Text(intensityLevel.displayName) },
                            onClick = {
                                intensity = intensityLevel
                                intensityExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // Save Button
        Button(
            onClick = {
                // Validate and save exercise log
                val durationInt = duration.toIntOrNull()

                if (durationInt != null && durationInt > 0) {
                    val exerciseLog = ExerciseLog(
                        id = "exercise_${System.currentTimeMillis()}",
                        dateTime = LocalDateTime.of(date, time),
                        period = period,
                        notes = notes.ifBlank { null },
                        durationMinutes = durationInt,
                        exerciseType = exerciseType,
                        intensity = intensity
                    )

                    // Here you would save to your data store
                    // For now, just show a success message and reset
                    notes = ""
                    duration = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = duration.isNotBlank() && duration.toIntOrNull() != null
        ) {
            Text("Save Exercise Log")
        }
    }

    // Date and Time Picker Dialogs
    if (showDatePicker) {
        DatePickerDialog(
            date = date,
            onDateSelected = { selectedDate ->
                date = selectedDate
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            time = time,
            onTimeSelected = { selectedTime ->
                time = selectedTime
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun MedicationLogForm() {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var period by remember { mutableStateOf(EntryPeriod.BEFORE_BREAKFAST) }
    var notes by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var dosageUnit by remember { mutableStateOf("mg") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Date and Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = date.format(dateFormatter),
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("📅")
                    }
                }
            )
            OutlinedTextField(
                value = time.format(timeFormatter),
                onValueChange = { },
                label = { Text("Time") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showTimePicker = true }) {
                        Text("🕐")
                    }
                }
            )
        }

        // Period
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = period.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Period") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                EntryPeriod.values().forEach { entryPeriod ->
                    DropdownMenuItem(
                        text = { Text(entryPeriod.displayName) },
                        onClick = {
                            period = entryPeriod
                            expanded = false
                        }
                    )
                }
            }
        }

        // Medication Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Medication Name *") },
            modifier = Modifier.fillMaxWidth(),
            isError = name.isBlank()
        )

        // Dosage and Unit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage *") },
                modifier = Modifier.weight(1f),
                isError = dosage.isBlank()
            )
            OutlinedTextField(
                value = dosageUnit,
                onValueChange = { dosageUnit = it },
                label = { Text("Unit") },
                modifier = Modifier.weight(1f),
                supportingText = { Text("e.g., mg, IU, ml") }
            )
        }

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // Save Button
        Button(
            onClick = {
                // Validate and save medication log
                val dosageFloat = dosage.toFloatOrNull()

                if (name.isNotBlank() && dosageFloat != null && dosageFloat > 0) {
                    val medicationLog = MedicationLog(
                        id = "med_${System.currentTimeMillis()}",
                        dateTime = LocalDateTime.of(date, time),
                        period = period,
                        notes = notes.ifBlank { null },
                        name = name,
                        dosage = dosageFloat,
                        dosageUnit = dosageUnit
                    )

                    // Here you would save to your data store
                    // For now, just show a success message and reset
                    notes = ""
                    name = ""
                    dosage = ""
                    dosageUnit = "mg"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && dosage.isNotBlank() && dosage.toFloatOrNull() != null
        ) {
            Text("Save Medication Log")
        }
    }

    // Date and Time Picker Dialogs
    if (showDatePicker) {
        DatePickerDialog(
            date = date,
            onDateSelected = { selectedDate ->
                date = selectedDate
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            time = time,
            onTimeSelected = { selectedTime ->
                time = selectedTime
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
