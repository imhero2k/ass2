package com.example.fit5046_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fit5046_app.model.BloodType
import com.example.fit5046_app.model.Gender
import com.example.fit5046_app.model.HealthCondition
import com.example.fit5046_app.model.MeasurementUnits
import com.example.fit5046_app.model.Relationship

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthStep(
    name: String,
    email: String,
    password: String,
    passwordVisible: Boolean,
    authState: AuthState,
    authViewModel: AuthViewModel,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
    onBackToStart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        /* ── teal header section ──────────────────────────────────────── */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = 50.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Register",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(56.dp)) // Balance the back button
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(32.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Full Name *", color = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !authState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email *", color = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !authState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password *", color = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = onPasswordVisibilityToggle) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !authState.isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Register button
                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    enabled = !authState.isLoading && name.isNotBlank() && email.isNotBlank() && password.length >= 6
                ) {
                    if (authState.isLoading) {
                        Text("Creating Account...", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    } else {
                        Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Error message
                authState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.DarkGray.copy(alpha = 0.7f))) {
                            append("Already have an account? ")
                        }
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.Underline
                            )
                        ) { append("Log in") }
                    },
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onBackClick() }
                )

                Spacer(Modifier.height(32.dp))

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedRegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onBackToStart: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    // Use a simple remember with key based on auth state
    val shouldShowOnboarding = remember(authState.user) {
        val result = authState.user != null
        println("DEBUG: shouldShowOnboarding calculated: authState.user=${authState.user?.email}, result=$result")
        println("DEBUG: authState.user details: ${authState.user?.uid}, ${authState.user?.email}")
        result
    } // false: Auth, true: Onboarding

    // Track auth state changes
    LaunchedEffect(authState.user) {
        println("DEBUG: EnhancedRegisterScreen LaunchedEffect triggered: authState.user=${authState.user?.email}")
    }

    // Auth form state
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Profile form state (for passing to OnboardingFlow)
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.OTHER) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf(BloodType.UNKNOWN) }
    var healthConditions by remember { mutableStateOf(listOf<HealthCondition>()) }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var emergencyContactRelationship by remember { mutableStateOf(Relationship.OTHER) }
    var units by remember { mutableStateOf(MeasurementUnits.METRIC) }

    // Handle successful registration - populate form fields when user is created
    LaunchedEffect(authState.user) {
        if (authState.user != null) {
            println("DEBUG: User created, populating form fields")
            // Split name into first and last name
            val nameParts = name.trim().split(" ")
            firstName = nameParts.firstOrNull() ?: ""
            lastName = nameParts.drop(1).joinToString(" ")
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        println("DEBUG: About to render, authState.user = ${authState.user?.email}, shouldShowOnboarding = $shouldShowOnboarding")

        if (!shouldShowOnboarding) {
            println("DEBUG: Rendering AuthStep (currentStep = 0)")
            // Authentication Step
            AuthStep(
                name = name,
                email = email,
                password = password,
                passwordVisible = passwordVisible,
                authState = authState,
                authViewModel = authViewModel,
                onNameChange = { name = it },
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                onRegisterClick = { authViewModel.signUp(name, email, password) },
                onBackClick = onBackClick,
                onBackToStart = onBackToStart
            )
        } else {
            println("DEBUG: Rendering OnboardingFlow (currentStep = 1)")
            // Onboarding Step
            OnboardingFlow(
                onComplete = {
                    println("DEBUG: OnboardingFlow onComplete called - this should only happen when user clicks Complete Setup")
                    // The OnboardingFlow will handle saving the profile
                    // We just need to navigate to the main app
                    onRegisterSuccess()
                }
            )
        }
    }

    // Track when EnhancedRegisterScreen is disposed
    DisposableEffect(Unit) {
        println("DEBUG: EnhancedRegisterScreen DisposableEffect onStart")
        onDispose {
            println("DEBUG: EnhancedRegisterScreen DisposableEffect onDispose")
        }
    }
}