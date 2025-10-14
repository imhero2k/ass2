@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fit5046_app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fit5046_app.model.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fit5046_app.data.BloodGlucoseLogEntity
import com.example.fit5046_app.data.DietLogEntity
import com.example.fit5046_app.data.ExerciseLogEntity
import com.example.fit5046_app.data.MedicationLogEntity
import com.example.fit5046_app.model.DietaryPreference
import com.example.fit5046_app.model.EntryPeriod
import com.example.fit5046_app.model.ExerciseType
import com.example.fit5046_app.model.Intensity
import com.example.fit5046_app.model.MealType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Log(
    navController: androidx.navigation.NavController? = null,
    editingLogId: Int? = null,
    editingLogType: String? = null
) {
    val context = LocalContext.current
    val logViewModel: LogViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )

    // Get current user ID from Firebase Auth
    val currentUserId =
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    // Track the editing log ID and type (can be cleared when form is collapsed)
    var currentEditingLogId by remember { mutableStateOf(editingLogId) }
    var currentEditingLogType by remember { mutableStateOf(editingLogType) }

    // If editing a log, expand the corresponding section automatically
    var expandedDiet by remember { mutableStateOf(editingLogType == "DIET") }
    var expandedBloodGlucose by remember { mutableStateOf(editingLogType == "BLOOD_GLUCOSE") }
    var expandedExercise by remember { mutableStateOf(editingLogType == "EXERCISE") }
    var expandedMedication by remember { mutableStateOf(editingLogType == "MEDICATION") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Diet Log Section
        ExpandableLogSection(
            title = "Diet Log",
            logIcon = painterResource(id = R.drawable.diet_24px),
            icon = Icons.Default.Add,
            isExpanded = expandedDiet,
            onToggle = {
                expandedDiet = !expandedDiet
                // Clear editing state when collapsing to reset form
                if (!expandedDiet && currentEditingLogType == "DIET") {
                    currentEditingLogId = null
                    currentEditingLogType = null
                }
            }
        ) {
            DietLogForm(
                userId = currentUserId,
                logViewModel = logViewModel,
                navController = navController,
                editingLogId = if (currentEditingLogType == "DIET") currentEditingLogId else null
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Blood Glucose Log Section
        ExpandableLogSection(
            title = "Blood Glucose Log",
            logIcon = painterResource(id = R.drawable.water_drop_24px),
            icon = Icons.Default.Add,
            isExpanded = expandedBloodGlucose,
            onToggle = {
                expandedBloodGlucose = !expandedBloodGlucose
                // Clear editing state when collapsing to reset form
                if (!expandedBloodGlucose && currentEditingLogType == "BLOOD_GLUCOSE") {
                    currentEditingLogId = null
                    currentEditingLogType = null
                }
            }
        ) {
            BloodGlucoseLogForm(
                userId = currentUserId,
                logViewModel = logViewModel,
                navController = navController,
                editingLogId = if (currentEditingLogType == "BLOOD_GLUCOSE") currentEditingLogId else null
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Exercise Log Section
        ExpandableLogSection(
            title = "Exercise Log",
            logIcon = painterResource(id = R.drawable.exercise_24px),
            icon = Icons.Default.Add,
            isExpanded = expandedExercise,
            onToggle = {
                expandedExercise = !expandedExercise
                // Clear editing state when collapsing to reset form
                if (!expandedExercise && currentEditingLogType == "EXERCISE") {
                    currentEditingLogId = null
                    currentEditingLogType = null
                }
            }
        ) {
            ExerciseLogForm(
                userId = currentUserId,
                logViewModel = logViewModel,
                navController = navController,
                editingLogId = if (currentEditingLogType == "EXERCISE") currentEditingLogId else null
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Medication Log Section
        ExpandableLogSection(
            title = "Medication Log",
            logIcon = painterResource(id = R.drawable.med_24px),
            icon = Icons.Default.Add,
            isExpanded = expandedMedication,
            onToggle = {
                expandedMedication = !expandedMedication
                // Clear editing state when collapsing to reset form
                if (!expandedMedication && currentEditingLogType == "MEDICATION") {
                    currentEditingLogId = null
                    currentEditingLogType = null
                }
            }
        ) {
            MedicationLogForm(
                userId = currentUserId,
                logViewModel = logViewModel,
                navController = navController,
                editingLogId = if (currentEditingLogType == "MEDICATION") currentEditingLogId else null
            )
        }
    }
}


@Composable
fun ExpandableLogSection(
    title: String,
    logIcon: Painter,
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
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header with title and expand/collapse button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = logIcon,
                        contentDescription = "Log Type Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

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
        initialSelectedDateMillis = date.atStartOfDay().atZone(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
    )

    androidx.compose.material3.DatePickerDialog(
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
        },
        modifier = Modifier.padding(16.dp)
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
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
fun DietLogForm(
    userId: String,
    logViewModel: LogViewModel,
    navController: androidx.navigation.NavController?,
    editingLogId: Int? = null
) {
    val context = LocalContext.current
    var existingLog by remember { mutableStateOf<DietLogEntity?>(null) }

    // Initialize form fields
    var date by remember(editingLogId) { mutableStateOf(LocalDate.now()) }
    var time by remember(editingLogId) { mutableStateOf(LocalTime.now()) }
    var period by remember(editingLogId) { mutableStateOf(EntryPeriod.BREAKFAST) }
    var notes by remember(editingLogId) { mutableStateOf("") }
    var calories by remember(editingLogId) { mutableStateOf("") }
    var protein by remember(editingLogId) { mutableStateOf("") }
    var fat by remember(editingLogId) { mutableStateOf("") }
    var carbs by remember(editingLogId) { mutableStateOf("") }
    var newFoodName by remember(editingLogId) { mutableStateOf("") }
    var selectedMealType by remember(editingLogId) { mutableStateOf(MealType.HOMEMADE) }
    var selectedDietaryPreferences by remember(editingLogId) { mutableStateOf(setOf<DietaryPreference>()) }

    // Load existing log if editing and populate form fields
    LaunchedEffect(editingLogId) {
        if (editingLogId != null) {
            val log = logViewModel.getDietLogById(editingLogId)
            existingLog = log

            // Populate form fields with loaded data
            log?.let {
                date = it.dateTime.toLocalDate()
                time = it.dateTime.toLocalTime()
                period = it.period
                notes = it.notes ?: ""
                calories = it.calories?.toString() ?: ""
                protein = it.proteinGrams?.toString() ?: ""
                fat = it.fatGrams?.toString() ?: ""
                carbs = it.carbGrams?.toString() ?: ""
                newFoodName = it.foodItems?.joinToString(", ") ?: ""
                selectedMealType = it.mealType ?: MealType.HOMEMADE
                selectedDietaryPreferences = it.dietaryPreference?.toSet() ?: setOf()
            }
        } else {
            existingLog = null
        }
    }

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
                modifier = Modifier.weight(0.6f),
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
                modifier = Modifier.weight(0.4f),
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
                onDismissRequest = { expanded = false },
                containerColor = Color.White
            ) {
                EntryPeriod.entries.forEach { entryPeriod ->
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

        // Meal Type Selection (Radio Buttons)
        Text(
            text = "Meal Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            MealType.entries.forEach { mealType ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMealType == mealType,
                        onClick = { selectedMealType = mealType }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mealType.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Dietary Preferences (Checkboxes)
        Text(
            text = "Dietary Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            DietaryPreference.entries.toList().chunked(2).forEach { rowPreferences ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowPreferences.forEach { preference ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = selectedDietaryPreferences.contains(preference),
                                onCheckedChange = { isChecked ->
                                    selectedDietaryPreferences = if (isChecked) {
                                        selectedDietaryPreferences + preference
                                    } else {
                                        selectedDietaryPreferences - preference
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = preference.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    // Add empty space if odd number of items in row
                    if (rowPreferences.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
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

        // Camera and Gallery Section
        Text(
            text = "Food Recognition",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Take a photo or select from gallery to automatically detect food items and fill nutritional information",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Camera Button
            OutlinedButton(
                onClick = {
                    // TODO: Implement camera functionality
                    // This will trigger ML model to analyze food and fill form
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Take Photo",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Photo")
            }

            // Gallery Button
            OutlinedButton(
                onClick = {
                    // TODO: Implement gallery functionality
                    // This will trigger ML model to analyze food and fill form
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Select from Gallery",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gallery")
            }
        }

        // ML Analysis Status (placeholder for future implementation)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "ML Analysis",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI Food Recognition",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Take a photo to automatically detect food items and estimate nutritional values",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

        // Save/Update/Delete Buttons
        if (editingLogId != null) {
            existingLog?.let { logToEdit ->
                // Update Button
                Button(
                    onClick = {
                        // Validate and update diet log
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
                            } else {
                                emptyList()
                            }

                            // Update existing DietLogEntity
                            val updatedDietLogEntity = logToEdit.copy(
                                dateTime = LocalDateTime.of(date, time),
                                period = period,
                                notes = notes.ifBlank { null },
                                mealType = selectedMealType,
                                dietaryPreference = selectedDietaryPreferences.toList(),
                                calories = caloriesInt,
                                proteinGrams = proteinFloat,
                                fatGrams = fatFloat,
                                carbGrams = carbsFloat,
                                foodItems = parsedFoodItems
                            )

                            // Update in database
                            logViewModel.updateDietLog(updatedDietLogEntity)

                            // Show toast
                            android.widget.Toast.makeText(
                                context,
                                "Diet log updated successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()

                            // Navigate to Diary screen
                            navController?.navigate("diary") {
                                popUpTo("log") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = calories.isNotBlank() && protein.isNotBlank() && fat.isNotBlank() && carbs.isNotBlank()
                ) {
                    Text("Update Diet Log")
                }

                // Delete Button
                Button(
                    onClick = {
                        // Delete the log
                        logViewModel.deleteDietLog(logToEdit)

                        // Show toast
                        android.widget.Toast.makeText(
                            context,
                            "Diet log deleted successfully",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to Diary screen
                        navController?.navigate("diary") {
                            popUpTo("log") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Delete Diet Log")
                }
            }
        } else {
            // Save Button (for new logs)
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
                        } else {
                            emptyList()
                        }

                        // Create DietLogEntity to save to database
                        val dietLogEntity = DietLogEntity(
                            userId = userId,
                            dateTime = LocalDateTime.of(date, time),
                            period = period,
                            notes = notes.ifBlank { null },
                            mealType = selectedMealType,
                            dietaryPreference = selectedDietaryPreferences.toList(),
                            calories = caloriesInt,
                            proteinGrams = proteinFloat,
                            fatGrams = fatFloat,
                            carbGrams = carbsFloat,
                            foodItems = parsedFoodItems
                        )

                        // Save to database
                        logViewModel.insertDietLog(dietLogEntity)

                        // Show toast
                        android.widget.Toast.makeText(
                            context,
                            "Diet log saved successfully",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        // Navigate to Diary screen
                        navController?.navigate("diary") {
                            popUpTo("diary") { inclusive = false }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = calories.isNotBlank() && protein.isNotBlank() && fat.isNotBlank() && carbs.isNotBlank()
            ) {
                Text("Save Diet Log")
            }
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
fun BloodGlucoseLogForm(
    userId: String,
    logViewModel: LogViewModel,
    navController: androidx.navigation.NavController?,
    editingLogId: Int? = null
) {
    val context = LocalContext.current
    var existingLog by remember { mutableStateOf<BloodGlucoseLogEntity?>(null) }

    // Load existing log if editing
    LaunchedEffect(editingLogId) {
        if (editingLogId != null) {
            existingLog = logViewModel.getBloodGlucoseLogById(editingLogId)
        } else {
            existingLog = null
        }
    }

    // Initialize form fields
    var date by remember(editingLogId) { mutableStateOf(LocalDate.now()) }
    var time by remember(editingLogId) { mutableStateOf(LocalTime.now()) }
    var period by remember(editingLogId) { mutableStateOf(EntryPeriod.BEFORE_BREAKFAST) }
    var notes by remember(editingLogId) { mutableStateOf("") }
    var value by remember(editingLogId) { mutableStateOf("") }

    // Populate form fields when data loads
    LaunchedEffect(existingLog) {
        existingLog?.let {
            date = it.dateTime.toLocalDate()
            time = it.dateTime.toLocalTime()
            period = it.period
            notes = it.notes ?: ""
            value = it.value.toString()
        }
    }

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
                modifier = Modifier.weight(0.6f),
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
                modifier = Modifier.weight(0.4f),
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
                onDismissRequest = { expanded = false },
                containerColor = Color.White
            ) {
                EntryPeriod.entries.forEach { entryPeriod ->
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

        // Save/Update/Delete Buttons
        if (editingLogId != null) {
            existingLog?.let { logToEdit ->
                // Update Button
                Button(
                    onClick = {
                        val glucoseValue = value.toFloatOrNull()

                        if (glucoseValue != null && glucoseValue > 0) {
                            val updatedLog = logToEdit.copy(
                                dateTime = LocalDateTime.of(date, time),
                                period = period,
                                notes = notes.ifBlank { null },
                                value = glucoseValue
                            )

                            logViewModel.updateBloodGlucoseLog(updatedLog)
                            android.widget.Toast.makeText(
                                context,
                                "Blood glucose log updated successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()

                            navController?.navigate("diary") {
                                popUpTo("log") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = value.isNotBlank() && value.toFloatOrNull() != null
                ) {
                    Text("Update Blood Glucose Log")
                }

                // Delete Button
                Button(
                    onClick = {
                        logViewModel.deleteBloodGlucoseLog(logToEdit)
                        android.widget.Toast.makeText(
                            context,
                            "Blood glucose log deleted successfully",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        navController?.navigate("diary") {
                            popUpTo("log") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Delete Blood Glucose Log")
                }
            }
        } else {
            // Save Button (for new logs)
            Button(
                onClick = {
                    // Validate and save blood glucose log
                    val glucoseValue = value.toFloatOrNull()

                    if (glucoseValue != null && glucoseValue > 0) {
                        // Create BloodGlucoseLogEntity to save to database
                        val bloodGlucoseLogEntity = BloodGlucoseLogEntity(
                            userId = userId,
                            dateTime = LocalDateTime.of(date, time),
                            period = period,
                            notes = notes.ifBlank { null },
                            value = glucoseValue
                        )

                        // Save to database
                        logViewModel.insertBloodGlucoseLog(bloodGlucoseLogEntity)

                        // Show toast
                        android.widget.Toast.makeText(
                            context,
                            "Blood glucose log saved successfully",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to Diary screen
                        navController?.navigate("diary") {
                            popUpTo("log") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = value.isNotBlank() && value.toFloatOrNull() != null
            ) {
                Text("Save Blood Glucose Log")
            }
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
fun ExerciseLogForm(
    userId: String,
    logViewModel: LogViewModel,
    navController: androidx.navigation.NavController?,
    editingLogId: Int? = null
) {
    val context = LocalContext.current
    var existingLog by remember { mutableStateOf<ExerciseLogEntity?>(null) }

    // Initialize form fields
    var date by remember(editingLogId) { mutableStateOf(LocalDate.now()) }
    var time by remember(editingLogId) { mutableStateOf(LocalTime.now()) }
    var period by remember(editingLogId) { mutableStateOf(EntryPeriod.MORNING) }
    var notes by remember(editingLogId) { mutableStateOf("") }
    var duration by remember(editingLogId) { mutableStateOf("") }
    var exerciseType by remember(editingLogId) { mutableStateOf(ExerciseType.RUNNING) }
    var intensity by remember(editingLogId) { mutableStateOf(Intensity.MEDIUM) }

    // Load existing log if editing and populate form fields
    LaunchedEffect(editingLogId) {
        if (editingLogId != null) {
            val log = logViewModel.getExerciseLogById(editingLogId)
            existingLog = log

            log?.let {
                date = it.dateTime.toLocalDate()
                time = it.dateTime.toLocalTime()
                period = it.period
                notes = it.notes ?: ""
                duration = it.durationMinutes.toString()
                exerciseType = it.exerciseType
                intensity = it.intensity
            }
        } else {
            existingLog = null
        }
    }

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
                modifier = Modifier.weight(0.6f),
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
                modifier = Modifier.weight(0.4f),
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
                onDismissRequest = { periodExpanded = false },
                containerColor = Color.White
            ) {
                EntryPeriod.entries.forEach { entryPeriod ->
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
                onDismissRequest = { typeExpanded = false },
                containerColor = Color.White
            ) {
                ExerciseType.entries.forEach { type ->
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
                    onDismissRequest = { intensityExpanded = false },
                    containerColor = Color.White
                ) {
                    Intensity.entries.forEach { intensityLevel ->
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

        // Save/Update/Delete Buttons
        if (editingLogId != null) {
            existingLog?.let { logToEdit ->
                // Update Button
                Button(
                    onClick = {
                        val durationInt = duration.toIntOrNull()

                        if (durationInt != null && durationInt > 0) {
                            val updatedLog = logToEdit.copy(
                                dateTime = LocalDateTime.of(date, time),
                                period = period,
                                notes = notes.ifBlank { null },
                                durationMinutes = durationInt,
                                exerciseType = exerciseType,
                                intensity = intensity
                            )

                            logViewModel.updateExerciseLog(updatedLog)
                            android.widget.Toast.makeText(
                                context,
                                "Exercise log updated successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()

                            navController?.navigate("diary") {
                                popUpTo("log") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = duration.isNotBlank() && duration.toIntOrNull() != null
                ) {
                    Text("Update Exercise Log")
                }

                // Delete Button
                Button(
                    onClick = {
                        logViewModel.deleteExerciseLog(logToEdit)
                        android.widget.Toast.makeText(
                            context,
                            "Exercise log deleted successfully",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        navController?.navigate("diary") {
                            popUpTo("log") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Delete Exercise Log")
                }
            }
        } else {
            // Save Button (for new logs)
            Button(
                onClick = {
                    // Validate and save exercise log
                    val durationInt = duration.toIntOrNull()

                    if (durationInt != null && durationInt > 0) {
                        // Create ExerciseLogEntity to save to database
                        val exerciseLogEntity = ExerciseLogEntity(
                            userId = userId,
                            dateTime = LocalDateTime.of(date, time),
                            period = period,
                            notes = notes.ifBlank { null },
                            durationMinutes = durationInt,
                            exerciseType = exerciseType,
                            intensity = intensity
                        )

                        // Save to database
                        logViewModel.insertExerciseLog(exerciseLogEntity)

                        // Show toast
                        android.widget.Toast.makeText(
                            context,
                            "Exercise log saved successfully",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to Diary screen
                        navController?.navigate("diary") {
                            popUpTo("log") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = duration.isNotBlank() && duration.toIntOrNull() != null
            ) {
                Text("Save Exercise Log")
            }
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
fun MedicationLogForm(
    userId: String,
    logViewModel: LogViewModel,
    navController: androidx.navigation.NavController?,
    editingLogId: Int? = null
) {
    val context = LocalContext.current
    var existingLog by remember { mutableStateOf<MedicationLogEntity?>(null) }

    // Initialize form fields
    var date by remember(editingLogId) { mutableStateOf(LocalDate.now()) }
    var time by remember(editingLogId) { mutableStateOf(LocalTime.now()) }
    var period by remember(editingLogId) { mutableStateOf(EntryPeriod.BEFORE_BREAKFAST) }
    var notes by remember(editingLogId) { mutableStateOf("") }
    var name by remember(editingLogId) { mutableStateOf("") }
    var dosage by remember(editingLogId) { mutableStateOf("") }
    var dosageUnit by remember(editingLogId) { mutableStateOf("mg") }

    // Load existing log if editing and populate form fields
    LaunchedEffect(editingLogId) {
        if (editingLogId != null) {
            val log = logViewModel.getMedicationLogById(editingLogId)
            existingLog = log

            log?.let {
                date = it.dateTime.toLocalDate()
                time = it.dateTime.toLocalTime()
                period = it.period
                notes = it.notes ?: ""
                name = it.name
                dosage = it.dosage.toString()
                dosageUnit = it.dosageUnit
            }
        } else {
            existingLog = null
        }
    }

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
                modifier = Modifier.weight(0.6f),
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
                modifier = Modifier.weight(0.4f),
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
                onDismissRequest = { expanded = false },
                containerColor = Color.White
            ) {
                EntryPeriod.entries.forEach { entryPeriod ->
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

        // Save/Update/Delete Buttons
        if (editingLogId != null) {
            existingLog?.let { logToEdit ->
                // Update Button
                Button(
                    onClick = {
                        val dosageFloat = dosage.toFloatOrNull()

                        if (name.isNotBlank() && dosageFloat != null && dosageFloat > 0) {
                            val updatedLog = logToEdit.copy(
                                dateTime = LocalDateTime.of(date, time),
                                period = period,
                                notes = notes.ifBlank { null },
                                name = name,
                                dosage = dosageFloat,
                                dosageUnit = dosageUnit
                            )

                            logViewModel.updateMedicationLog(updatedLog)
                            android.widget.Toast.makeText(
                                context,
                                "Medication log updated successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()

                            navController?.navigate("diary") {
                                popUpTo("log") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && dosage.isNotBlank() && dosage.toFloatOrNull() != null
                ) {
                    Text("Update Medication Log")
                }

                // Delete Button
                Button(
                    onClick = {
                        logViewModel.deleteMedicationLog(logToEdit)
                        android.widget.Toast.makeText(
                            context,
                            "Medication log deleted successfully",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        navController?.navigate("diary") {
                            popUpTo("log") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Delete Medication Log")
                }
            }
        } else {
            // Save Button (for new logs)
            Button(
                onClick = {
                    // Validate and save medication log
                    val dosageFloat = dosage.toFloatOrNull()

                    if (name.isNotBlank() && dosageFloat != null && dosageFloat > 0) {
                        // Create MedicationLogEntity to save to database
                        val medicationLogEntity = MedicationLogEntity(
                            userId = userId,
                            dateTime = LocalDateTime.of(date, time),
                            period = period,
                            notes = notes.ifBlank { null },
                            name = name,
                            dosage = dosageFloat,
                            dosageUnit = dosageUnit
                        )

                        // Save to database
                        logViewModel.insertMedicationLog(medicationLogEntity)

                        // Show toast
                        android.widget.Toast.makeText(
                            context,
                            "Medication log saved successfully",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to Diary screen
                        navController?.navigate("diary") {
                            popUpTo("log") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && dosage.isNotBlank() && dosage.toFloatOrNull() != null
            ) {
                Text("Save Medication Log")
            }
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
