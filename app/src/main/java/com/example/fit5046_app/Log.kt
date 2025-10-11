@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fit5046_app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fit5046_app.model.BloodGlucoseLog
import com.example.fit5046_app.model.DietLog
import com.example.fit5046_app.model.EntryPeriod
import com.example.fit5046_app.model.ExerciseLog
import com.example.fit5046_app.model.ExerciseType
import com.example.fit5046_app.model.FoodItemModel
import com.example.fit5046_app.model.Intensity
import com.example.fit5046_app.model.MedicationLog
import com.example.fit5046_app.ui.theme.FoodSearchComponent
import com.example.fit5046_app.ui.theme.FoodItemWithQuantity
import com.example.fit5046_app.ui.theme.calculateCalories
import com.example.fit5046_app.ui.theme.calculateProtein
import com.example.fit5046_app.ui.theme.calculateCarbs
import com.example.fit5046_app.ui.theme.calculateFat
import com.example.fit5046_app.ui.theme.NutritionInfo
import com.example.fit5046_app.ui.theme.EnhancedNutritionCard
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Log() {
    var expandedDiet by remember { mutableStateOf(false) }
    var expandedBloodGlucose by remember { mutableStateOf(false) }
    var expandedExercise by remember { mutableStateOf(false) }
    var expandedMedication by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Add New Log Entry",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Diet Log Section
        ExpandableLogSection(
            title = "Diet Log",
            logIcon = painterResource(id = R.drawable.diet_24px),
            icon = Icons.Default.Add,
            isExpanded = expandedDiet,
            onToggle = { expandedDiet = !expandedDiet }
        ) {
            DietLogForm()
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Blood Glucose Log Section
        ExpandableLogSection(
            title = "Blood Glucose Log",
            logIcon = painterResource(id = R.drawable.water_drop_24px),
            icon = Icons.Default.Add,
            isExpanded = expandedBloodGlucose,
            onToggle = { expandedBloodGlucose = !expandedBloodGlucose }
        ) {
            BloodGlucoseLogForm()
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Exercise Log Section
        ExpandableLogSection(
            title = "Exercise Log",
            logIcon = painterResource(id = R.drawable.exercise_24px),
            icon = Icons.Default.Add,
            isExpanded = expandedExercise,
            onToggle = { expandedExercise = !expandedExercise }
        ) {
            ExerciseLogForm()
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Medication Log Section
        ExpandableLogSection(
            title = "Medication Log",
            logIcon = painterResource(id = R.drawable.med_24px),
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
    var selectedFoods by remember { mutableStateOf<List<FoodItemWithQuantity>>(emptyList()) }

    // Radio button states for meal type
    var selectedMealType by remember { mutableStateOf("Home Cooked") }
    val mealTypes = listOf("Home Cooked", "Restaurant", "Takeout", "Fast Food", "Snack")

    // Checkbox states for dietary preferences
    var isVegetarian by remember { mutableStateOf(false) }
    var isVegan by remember { mutableStateOf(false) }
    var isGlutenFree by remember { mutableStateOf(false) }
    var isLowCarb by remember { mutableStateOf(false) }
    var isLowFat by remember { mutableStateOf(false) }

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

        // Meal Type Selection (Radio Buttons)
        Text(
            text = "Meal Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            mealTypes.forEach { mealType ->
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
                        text = mealType,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = isVegetarian,
                        onCheckedChange = { isVegetarian = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vegetarian",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = isVegan,
                        onCheckedChange = { isVegan = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vegan",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = isGlutenFree,
                        onCheckedChange = { isGlutenFree = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gluten Free",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = isLowCarb,
                        onCheckedChange = { isLowCarb = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Low Carb",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = isLowFat,
                        onCheckedChange = { isLowFat = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Low Fat",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Food Search and Selection
        Text(
            text = "Food Selection",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        FoodSearchComponent(
            selectedFoods = selectedFoods,
            onFoodSelected = { foodWithQuantity ->
                selectedFoods = selectedFoods + foodWithQuantity
            },
            onFoodRemoved = { foodId ->
                selectedFoods = selectedFoods.filter { it.food.id != foodId }
            },
            onQuantityChanged = { foodId, quantity ->
                selectedFoods = selectedFoods.map {
                    if (it.food.id == foodId) {
                        it.copy(quantity = quantity)
                    } else {
                        it
                    }
                }
            },
            onCustomFoodAdded = { customFood ->
                selectedFoods = selectedFoods + FoodItemWithQuantity(customFood, 100f)
            }
        )

        // Enhanced Auto-calculated Nutritional Information
        if (selectedFoods.isNotEmpty()) {
            val totalCalories = selectedFoods.sumOf { calculateCalories(it).toDouble() }.toInt()
            val totalProtein = selectedFoods.sumOf { calculateProtein(it).toDouble() }.toFloat()
            val totalCarbs = selectedFoods.sumOf { calculateCarbs(it).toDouble() }.toFloat()
            val totalFat = selectedFoods.sumOf { calculateFat(it).toDouble() }.toFloat()

            EnhancedNutritionCard(
                calories = totalCalories,
                protein = totalProtein,
                carbs = totalCarbs,
                fat = totalFat,
                foodCount = selectedFoods.size
            )
        }

        // Manual Nutritional Information Override
        Text(
            text = "Manual Nutritional Override (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Use these fields to override or add to the auto-calculated values",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                label = { Text("Additional Calories") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it },
                label = { Text("Additional Protein (g)") },
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
                label = { Text("Additional Fat (g)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = it },
                label = { Text("Additional Carbs (g)") },
                modifier = Modifier.weight(1f)
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

        // Save Button
        Button(
            onClick = {
                // Calculate total nutrition values
                val autoCalories = if (selectedFoods.isNotEmpty()) {
                    selectedFoods.sumOf { calculateCalories(it).toDouble() }.toInt()
                } else 0
                val autoProtein = if (selectedFoods.isNotEmpty()) {
                    selectedFoods.sumOf { calculateProtein(it).toDouble() }.toFloat()
                } else 0f
                val autoFat = if (selectedFoods.isNotEmpty()) {
                    selectedFoods.sumOf { calculateFat(it).toDouble() }.toFloat()
                } else 0f
                val autoCarbs = if (selectedFoods.isNotEmpty()) {
                    selectedFoods.sumOf { calculateCarbs(it).toDouble() }.toFloat()
                } else 0f

                // Add manual overrides
                val additionalCalories = calories.toIntOrNull() ?: 0
                val additionalProtein = protein.toFloatOrNull() ?: 0f
                val additionalFat = fat.toFloatOrNull() ?: 0f
                val additionalCarbs = carbs.toFloatOrNull() ?: 0f

                val totalCalories = autoCalories + additionalCalories
                val totalProtein = autoProtein + additionalProtein
                val totalFat = autoFat + additionalFat
                val totalCarbs = autoCarbs + additionalCarbs

                // Parse comma-separated food items (legacy support)
                val parsedFoodItems = if (newFoodName.isNotBlank()) {
                    newFoodName.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { FoodItemModel(it, null) }
                } else {
                    emptyList()
                }

                // Add selected foods to food items
                val allFoodItems = parsedFoodItems + selectedFoods.map {
                    FoodItemModel(it.food.name, it.quantity.toString())
                }

                val dietLog = DietLog(
                    id = "diet_${System.currentTimeMillis()}",
                    dateTime = LocalDateTime.of(date, time),
                    period = period,
                    notes = notes.ifBlank { null },
                    calories = totalCalories,
                    proteinGrams = totalProtein,
                    fatGrams = totalFat,
                    carbGrams = totalCarbs,
                    foodItems = allFoodItems
                )

                // Here you would save to your data store
                // For now, just show a success message and reset
                notes = ""
                calories = ""
                protein = ""
                fat = ""
                carbs = ""
                newFoodName = ""
                selectedFoods = emptyList()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedFoods.isNotEmpty() || (calories.isNotBlank() && protein.isNotBlank() && fat.isNotBlank() && carbs.isNotBlank())
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

    // Radio button states for exercise location
    var selectedLocation by remember { mutableStateOf("Indoor") }
    val locations = listOf("Indoor", "Outdoor", "Gym", "Home", "Pool")

    // Checkbox states for exercise characteristics
    var withTrainer by remember { mutableStateOf(false) }
    var withMusic by remember { mutableStateOf(false) }
    var withGroup by remember { mutableStateOf(false) }
    var feltGood by remember { mutableStateOf(false) }

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
                onDismissRequest = { typeExpanded = false },
                containerColor = Color.White
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
