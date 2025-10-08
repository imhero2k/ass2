package com.example.fit5046_app

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthenticationScreen(
    onAuthSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var isSignUp by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var resetEmailSent by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    // Navigate to main app when user is authenticated
    LaunchedEffect(authState.user) {
        if (authState.user != null) {
            onAuthSuccess()
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
            SignUpScreen(
                onSignUpClick = { name, email, password ->
                    authViewModel.signUp(name, email, password)
                },
                onSignInClick = {
                    isSignUp = false
                    authViewModel.clearError()
                },
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage
            )
        }
        else -> {
            SignInScreen(
                onSignInClick = { email, password ->
                    authViewModel.signIn(email, password)
                },
                onSignUpClick = {
                    isSignUp = true
                    authViewModel.clearError()
                },
                onForgotPasswordClick = {
                    showForgotPassword = true
                    authViewModel.clearError()
                },
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage
            )
        }
    }
}
