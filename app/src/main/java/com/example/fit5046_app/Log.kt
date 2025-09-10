@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fit5046_app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
    var foodItems by remember { mutableStateOf(mutableListOf<FoodItemModel>()) }
    var newFoodName by remember { mutableStateOf("") }
    var newFoodQuantity by remember { mutableStateOf("") }

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
                value = date.toString(),
                onValueChange = { /* Date picker would be implemented here */ },
                label = { Text("Date") },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
            OutlinedTextField(
                value = time.toString(),
                onValueChange = { /* Time picker would be implemented here */ },
                label = { Text("Time") },
                modifier = Modifier.weight(1f),
                readOnly = true
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
                label = { Text("Calories") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it },
                label = { Text("Protein (g)") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it },
                label = { Text("Fat (g)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = it },
                label = { Text("Carbs (g)") },
                modifier = Modifier.weight(1f)
            )
        }

        // Food Items
        Text(
            text = "Food Items",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newFoodName,
                onValueChange = { newFoodName = it },
                label = { Text("Food Name") },
                modifier = Modifier.weight(2f)
            )
            OutlinedTextField(
                value = newFoodQuantity,
                onValueChange = { newFoodQuantity = it },
                label = { Text("Quantity") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (newFoodName.isNotBlank()) {
                        foodItems.add(FoodItemModel(newFoodName, newFoodQuantity.ifBlank { null }))
                        newFoodName = ""
                        newFoodQuantity = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Food Item")
            }
        }

        // Display added food items
        foodItems.forEach { foodItem ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = foodItem.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        foodItem.quantity?.let { quantity ->
                            Text(
                                text = quantity,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TextButton(
                        onClick = { foodItems.remove(foodItem) }
                    ) {
                        Text("Remove")
                    }
                }
            }
        }

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
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
                    val dietLog = DietLog(
                        id = "diet_${System.currentTimeMillis()}",
                        dateTime = LocalDateTime.of(date, time),
                        period = period,
                        notes = notes.ifBlank { null },
                        calories = caloriesInt,
                        proteinGrams = proteinFloat,
                        fatGrams = fatFloat,
                        carbGrams = carbsFloat,
                        foodItems = foodItems.toList()
                    )

                    // Here you would save to your data store
                    // For now, just show a success message and reset
                    notes = ""
                    calories = ""
                    protein = ""
                    fat = ""
                    carbs = ""
                    foodItems.clear()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = calories.isNotBlank() && protein.isNotBlank() && fat.isNotBlank() && carbs.isNotBlank()
        ) {
            Text("Save Diet Log")
        }
    }
}

@Composable
fun BloodGlucoseLogForm() {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var period by remember { mutableStateOf(EntryPeriod.BEFORE_BREAKFAST) }
    var notes by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

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
                value = date.toString(),
                onValueChange = { /* Date picker would be implemented here */ },
                label = { Text("Date") },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
            OutlinedTextField(
                value = time.toString(),
                onValueChange = { /* Time picker would be implemented here */ },
                label = { Text("Time") },
                modifier = Modifier.weight(1f),
                readOnly = true
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
            label = { Text("Blood Glucose Level (mmol/L)") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Normal range: 3.9 - 7.8 mmol/L") }
        )

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
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
                value = date.toString(),
                onValueChange = { /* Date picker would be implemented here */ },
                label = { Text("Date") },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
            OutlinedTextField(
                value = time.toString(),
                onValueChange = { /* Time picker would be implemented here */ },
                label = { Text("Time") },
                modifier = Modifier.weight(1f),
                readOnly = true
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
                label = { Text("Duration (minutes)") },
                modifier = Modifier.weight(1f)
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
            label = { Text("Notes") },
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
                value = date.toString(),
                onValueChange = { /* Date picker would be implemented here */ },
                label = { Text("Date") },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
            OutlinedTextField(
                value = time.toString(),
                onValueChange = { /* Time picker would be implemented here */ },
                label = { Text("Time") },
                modifier = Modifier.weight(1f),
                readOnly = true
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
            label = { Text("Medication Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Dosage and Unit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage") },
                modifier = Modifier.weight(1f)
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
            label = { Text("Notes") },
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
}
