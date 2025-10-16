package com.example.fit5046_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fit5046_app.AuthViewModel
import com.example.fit5046_app.AuthState
import com.example.fit5046_app.model.*
import com.example.fit5046_app.OnboardingFlow

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
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Join our health community",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !authState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !authState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
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
            enabled = !authState.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        if (authState.errorMessage != null) {
            Text(
                text = authState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Register button
        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !authState.isLoading && name.isNotBlank() && email.isNotBlank() && password.length >= 6
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBackClick) {
            Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Medium)
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