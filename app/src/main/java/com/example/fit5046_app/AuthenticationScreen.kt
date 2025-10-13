package com.example.fit5046_app

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthenticationScreen(
    onAuthSuccess: () -> Unit,
    onBackToStart: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    var isSignUp by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var resetEmailSent by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    // Navigate to main app when user is authenticated
    // BUT NOT during registration flow - let EnhancedRegisterScreen handle onboarding
    LaunchedEffect(authState.user, isSignUp) {
        println("DEBUG: AuthenticationScreen LaunchedEffect triggered: authState.user=${authState.user?.email}, isSignUp=$isSignUp")
        if (authState.user != null && !isSignUp) {
            println("DEBUG: AuthenticationScreen calling onAuthSuccess()")
            onAuthSuccess()
        } else {
            println("DEBUG: AuthenticationScreen NOT calling onAuthSuccess() - isSignUp=$isSignUp")
        }
    }

    // Handle password reset success
    LaunchedEffect(authState.isLoading) {
        if (!authState.isLoading && authState.errorMessage == null && showForgotPassword) {
            resetEmailSent = true
        }
    }

    when {
        showForgotPassword -> {
            ForgotPasswordScreen(
                onBackClick = {
                    showForgotPassword = false
                    resetEmailSent = false
                    authViewModel.clearError()
                },
                onSendResetClick = { email ->
                    authViewModel.sendPasswordResetEmail(email)
                },
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage,
                successMessage = if (resetEmailSent) "Password reset email sent! Check your inbox." else null
            )
        }
        isSignUp -> {
            EnhancedRegisterScreen(
                onRegisterSuccess = onAuthSuccess,
                onBackClick = {
                    isSignUp = false
                    authViewModel.clearError()
                },
                onBackToStart = onBackToStart
            )
        }
        else -> {
            LoginScreen(
                onLoginSuccess = onAuthSuccess,
                onRegisterClick = {
                    isSignUp = true
                    authViewModel.clearError()
                },
                onForgotClick = {
                    showForgotPassword = true
                    authViewModel.clearError()
                },
                onBackClick = onBackToStart
            )
        }
    }
}
