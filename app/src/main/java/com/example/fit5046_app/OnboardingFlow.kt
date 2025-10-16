package com.example.fit5046_app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fit5046_app.model.*
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFlow(
    onComplete: () -> Unit = {}
) {
    println("DEBUG: OnboardingFlow composable called")
    println("DEBUG: OnboardingFlow onComplete lambda: $onComplete")
    println("DEBUG: OnboardingFlow about to render UI")

    // Add UserProfileViewModel to save data
    val profileViewModel: UserProfileViewModel = viewModel()
    var isCompleted by remember { mutableStateOf(false) }

    // Only call onComplete when user explicitly completes the form
    LaunchedEffect(isCompleted) {
        println("DEBUG: OnboardingFlow LaunchedEffect triggered, isCompleted = $isCompleted")
        if (isCompleted) {
            println("DEBUG: OnboardingFlow calling onComplete()")
            onComplete()
        }
    }

    // Track when OnboardingFlow is disposed
    DisposableEffect(Unit) {
        println("DEBUG: OnboardingFlow DisposableEffect onStart")
        onDispose {
            println("DEBUG: OnboardingFlow DisposableEffect onDispose")
        }
    }

    val pagerState = rememberPagerState(pageCount = { 4 })
    var currentStep by remember { mutableStateOf(0) }

    // Handle page transitions
    LaunchedEffect(currentStep) {
        pagerState.animateScrollToPage(currentStep)
    }

    // Form state for all steps
    var dateOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.OTHER) }

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf(BloodType.UNKNOWN) }
    var healthConditions by remember { mutableStateOf(listOf<HealthCondition>()) }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var emergencyContactRelationship by remember { mutableStateOf(Relationship.OTHER) }
    var units by remember { mutableStateOf(MeasurementUnits.METRIC) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Progress Header
        OnboardingHeader(
            currentStep = currentStep,
            totalSteps = 4,
            onBackClick = {
                if (currentStep > 0) {
                    currentStep--
                }
            }
        )

        // Content Pages
        println("DEBUG: OnboardingFlow rendering HorizontalPager")
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            println("DEBUG: OnboardingFlow rendering page $page")
            when (page) {
                0 ->                 WelcomeStep(
                    onNext = {
                        currentStep++
                    }
                )
                1 ->                 PersonalInfoStep(
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    units = units,
                    selectedDate = selectedDate,
                    showDatePicker = showDatePicker,
                    onDateOfBirthChange = { dateOfBirth = it },
                    onGenderChange = { gender = it },
                    onUnitsChange = { units = it },
                    onDatePickerClick = { showDatePicker = true },
                    onNext = {
                        currentStep++
                    }
                )
                2 ->                 HealthInfoStep(
                    height = height,
                    weight = weight,
                    bloodType = bloodType,
                    healthConditions = healthConditions,
                    units = units,
                    onHeightChange = { height = it },
                    onWeightChange = { weight = it },
                    onBloodTypeChange = { bloodType = it },
                    onHealthConditionsChange = { healthConditions = it },
                    onNext = {
                        currentStep++
                    }
                )
                3 -> EmergencyContactStep(
                    emergencyContactName = emergencyContactName,
                    emergencyContactPhone = emergencyContactPhone,
                    emergencyContactRelationship = emergencyContactRelationship,
                    onEmergencyContactNameChange = { emergencyContactName = it },
                    onEmergencyContactPhoneChange = { emergencyContactPhone = it },
                    onEmergencyContactRelationshipChange = { emergencyContactRelationship = it },
                    onComplete = {
                        // Get user name from Firebase Auth
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val displayName = currentUser?.displayName ?: ""
                        val email = currentUser?.email ?: ""

                        // Split display name into first and last name
                        val nameParts = displayName.split(" ")
                        val firstName = nameParts.getOrNull(0) ?: ""
                        val lastName = nameParts.drop(1).joinToString(" ")

                        // Create UserProfile object with collected data
                        val userProfile = UserProfile(
                            personalInfo = PersonalInfo(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                dateOfBirth = dateOfBirth,
                                gender = gender
                            ),
                            healthInfo = HealthInfo(
                                height = height,
                                weight = weight,
                                bloodType = bloodType,
                                healthConditions = healthConditions,
                                emergencyContact = EmergencyContact(
                                    name = emergencyContactName,
                                    phoneNumber = emergencyContactPhone,
                                    relationship = emergencyContactRelationship.name
                                )
                            ),
                            preferences = UserPreferences(
                                units = units
                            )
                        )

                        // Save to Firestore
                        profileViewModel.saveUserProfile(userProfile)

                        isCompleted = true
                    }
                )
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            date = selectedDate,
            onDateSelected = { newDate ->
                selectedDate = newDate
                dateOfBirth = newDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingHeader(
    currentStep: Int,
    totalSteps: Int,
    onBackClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Text(
                text = "Step ${currentStep + 1} of $totalSteps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        // Progress Bar
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeStep(
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome Icon
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Welcome",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to HealthTracker!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Let's set up your profile to personalize your health journey. This will only take a few minutes.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoStep(
    dateOfBirth: String,
    gender: Gender,
    units: MeasurementUnits,
    selectedDate: LocalDate,
    showDatePicker: Boolean,
    onDateOfBirthChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onUnitsChange: (MeasurementUnits) -> Unit,
    onDatePickerClick: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Personal Information",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Tell us a bit about yourself",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Note: Name is already collected during registration

        OutlinedTextField(
            value = dateOfBirth.ifEmpty { selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) },
            onValueChange = onDateOfBirthChange,
            label = { Text("Date of Birth") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = onDatePickerClick) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDatePickerClick() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gender Dropdown
        var genderExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded }
        ) {
            OutlinedTextField(
                value = gender.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                Gender.values().forEach { genderOption ->
                    DropdownMenuItem(
                        text = { Text(genderOption.name) },
                        onClick = {
                            onGenderChange(genderOption)
                            genderExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Units Dropdown
        var unitsExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = unitsExpanded,
            onExpandedChange = { unitsExpanded = !unitsExpanded }
        ) {
            OutlinedTextField(
                value = units.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Measurement Units") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitsExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = unitsExpanded,
                onDismissRequest = { unitsExpanded = false }
            ) {
                MeasurementUnits.values().forEach { unitOption ->
                    DropdownMenuItem(
                        text = { Text(unitOption.name) },
                        onClick = {
                            onUnitsChange(unitOption)
                            unitsExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = dateOfBirth.isNotBlank()
        ) {
            Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthInfoStep(
    height: String,
    weight: String,
    bloodType: BloodType,
    healthConditions: List<HealthCondition>,
    units: MeasurementUnits,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onBloodTypeChange: (BloodType) -> Unit,
    onHealthConditionsChange: (List<HealthCondition>) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Health Information",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Help us understand your health profile",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Height and Weight
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = height,
                onValueChange = onHeightChange,
                label = { Text(if (units == MeasurementUnits.METRIC) "Height (cm)" else "Height (ft/in)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = { Text(if (units == MeasurementUnits.METRIC) "Weight (kg)" else "Weight (lbs)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Blood Type Dropdown
        var bloodTypeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = bloodTypeExpanded,
            onExpandedChange = { bloodTypeExpanded = !bloodTypeExpanded }
        ) {
            OutlinedTextField(
                value = bloodType.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Blood Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodTypeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = bloodTypeExpanded,
                onDismissRequest = { bloodTypeExpanded = false }
            ) {
                BloodType.values().forEach { bloodTypeOption ->
                    DropdownMenuItem(
                        text = { Text(bloodTypeOption.name.replace("_", " ")) },
                        onClick = {
                            onBloodTypeChange(bloodTypeOption)
                            bloodTypeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Health Conditions Multi-Select
        var healthConditionsExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = healthConditionsExpanded,
            onExpandedChange = { healthConditionsExpanded = !healthConditionsExpanded }
        ) {
            OutlinedTextField(
                value = if (healthConditions.isEmpty()) "None" else healthConditions.joinToString(", "),
                onValueChange = {},
                readOnly = true,
                label = { Text("Health Conditions (Optional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = healthConditionsExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = healthConditionsExpanded,
                onDismissRequest = { healthConditionsExpanded = false }
            ) {
                HealthCondition.values().forEach { condition ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(condition.name.replace("_", " "))
                                if (healthConditions.contains(condition)) {
                                    Text("✓", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        onClick = {
                            onHealthConditionsChange(
                                if (healthConditions.contains(condition)) {
                                    healthConditions - condition
                                } else {
                                    healthConditions + condition
                                }
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactStep(
    emergencyContactName: String,
    emergencyContactPhone: String,
    emergencyContactRelationship: Relationship,
    onEmergencyContactNameChange: (String) -> Unit,
    onEmergencyContactPhoneChange: (String) -> Unit,
    onEmergencyContactRelationshipChange: (Relationship) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Emergency Contact",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Who should we contact in case of emergency?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = emergencyContactName,
            onValueChange = onEmergencyContactNameChange,
            label = { Text("Emergency Contact Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = emergencyContactPhone,
            onValueChange = onEmergencyContactPhoneChange,
            label = { Text("Emergency Contact Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Relationship Dropdown
        var relationshipExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = relationshipExpanded,
            onExpandedChange = { relationshipExpanded = !relationshipExpanded }
        ) {
            OutlinedTextField(
                value = emergencyContactRelationship.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Relationship") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationshipExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = relationshipExpanded,
                onDismissRequest = { relationshipExpanded = false }
            ) {
                Relationship.values().forEach { relationshipOption ->
                    DropdownMenuItem(
                        text = { Text(relationshipOption.name) },
                        onClick = {
                            onEmergencyContactRelationshipChange(relationshipOption)
                            relationshipExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                onComplete()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = emergencyContactName.isNotBlank() && emergencyContactPhone.isNotBlank()
        ) {
            Text("Complete Setup", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}
