package com.example.fit5046_app

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fit5046_app.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfile(
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: UserProfileViewModel = viewModel()
    val profileState by viewModel.profileState.collectAsState()

    // Load profile on first composition
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // Personal Information State
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.OTHER) }
    var profileImageUrl by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf(BloodType.UNKNOWN) }
    var allergies by remember { mutableStateOf("") }
    var medications by remember { mutableStateOf("") }
    var healthConditions by remember { mutableStateOf(listOf<HealthCondition>()) }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var emergencyContactRelationship by remember { mutableStateOf(Relationship.OTHER) }
    var units by remember { mutableStateOf(MeasurementUnits.METRIC) }

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Update form fields when profile loads
    LaunchedEffect(profileState.profile) {
        profileState.profile?.let { profile ->
            firstName = profile.personalInfo.firstName
            lastName = profile.personalInfo.lastName
            email = profile.personalInfo.email
            phoneNumber = profile.personalInfo.phoneNumber
            dateOfBirth = profile.personalInfo.dateOfBirth
            // Parse date of birth if it exists
            if (profile.personalInfo.dateOfBirth.isNotEmpty()) {
                try {
                    selectedDate = LocalDate.parse(profile.personalInfo.dateOfBirth, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } catch (e: Exception) {
                    // If parsing fails, keep current selectedDate
                }
            }
            address = profile.personalInfo.address
            city = profile.personalInfo.city
            country = profile.personalInfo.country
            postalCode = profile.personalInfo.postalCode
            gender = profile.personalInfo.gender
            profileImageUrl = profile.personalInfo.profileImageUrl
            height = profile.healthInfo.height
            weight = profile.healthInfo.weight
            bloodType = profile.healthInfo.bloodType
            allergies = profile.healthInfo.allergies.joinToString(", ")
            medications = profile.healthInfo.medications.joinToString(", ")
            healthConditions = profile.healthInfo.healthConditions
            emergencyContactName = profile.healthInfo.emergencyContact.name
            emergencyContactPhone = profile.healthInfo.emergencyContact.phoneNumber
            emergencyContactRelationship = try {
                Relationship.valueOf(profile.healthInfo.emergencyContact.relationship)
            } catch (e: IllegalArgumentException) {
                Relationship.OTHER
            }
            units = profile.preferences.units
        }
    }

    // Show success message
    LaunchedEffect(profileState.isSaved) {
        if (profileState.isSaved) {
            viewModel.clearSaveStatus()
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Upload the selected image
            viewModel.uploadProfileImage(selectedUri)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Update Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Quick stats
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Profile Complete ✓",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Show error message if any
        profileState.errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Personal Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Profile Image Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") }
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUrl.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(profileImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Image",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                // Watermark overlay
                                if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Color.Black.copy(alpha = 0.3f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Text(
                                            text = "${firstName.take(1)}${lastName.take(1)}".uppercase(),
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Add Profile Image",
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add Photo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date of Birth with Date Picker
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    onValueChange = { },
                    label = { Text("Date of Birth") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
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
                                    gender = genderOption
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = { Text("Postal Code") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Health Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Health Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Measurement Units Dropdown
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
                                    units = unitOption
                                    unitsExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text(if (units == MeasurementUnits.METRIC) "Height (cm)" else "Height (ft/in)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
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
                                    bloodType = bloodTypeOption
                                    bloodTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Allergies (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = medications,
                    onValueChange = { medications = it },
                    label = { Text("Current Medications (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

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
                        label = { Text("Health Conditions") },
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
                                    healthConditions = if (healthConditions.contains(condition)) {
                                        healthConditions - condition
                                    } else {
                                        healthConditions + condition
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Emergency Contact Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Emergency Contact",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = emergencyContactName,
                    onValueChange = { emergencyContactName = it },
                    label = { Text("Emergency Contact Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = emergencyContactPhone,
                    onValueChange = { emergencyContactPhone = it },
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
                                    emergencyContactRelationship = relationshipOption
                                    relationshipExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Cancel, contentDescription = "Cancel", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel")
            }
            Button(
                onClick = {
                    // Create UserProfile object and save
                    val profile = UserProfile(
                        personalInfo = PersonalInfo(
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            phoneNumber = phoneNumber,
                            dateOfBirth = dateOfBirth,
                            gender = gender,
                            address = address,
                            city = city,
                            country = country,
                            postalCode = postalCode,
                            profileImageUrl = profileImageUrl
                        ),
                        healthInfo = HealthInfo(
                            height = height,
                            weight = weight,
                            bloodType = bloodType,
                            allergies = allergies.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            medications = medications.split(",").map { it.trim() }.filter { it.isNotEmpty() },
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
                    viewModel.saveUserProfile(profile)
                },
                enabled = !profileState.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (profileState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            }
        }
    }
}