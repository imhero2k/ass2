package com.example.fit5046_app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun UserSetting(
    authViewModel: AuthViewModel? = null,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToAppInfo: () -> Unit = {}
) {
    var showSignOutDialog by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "User",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Edit Profile Card
        SettingsCard(
            title = "Edit Profile",
            subtitle = "Update your personal information",
            icon = Icons.Default.Person,
            onClick = onNavigateToProfile
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Reminder Settings Card
        SettingsCard(
            title = "Reminder Settings",
            subtitle = "Manage medication reminders",
            icon = Icons.Default.Notifications,
            onClick = onNavigateToReminders
        )

        Spacer(modifier = Modifier.height(16.dp))

        // About Card
        SettingsCard(
            title = "About",
            subtitle = "App information and disclaimer",
            icon = Icons.Default.Info,
            onClick = onNavigateToAppInfo
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Log Out Card
        SettingsCard(
            title = "Log Out",
            subtitle = "Sign out of your account",
            icon = Icons.Default.ExitToApp,
            onClick = { showSignOutDialog = true },
            isDestructive = true
        )
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel?.signOut()
                        showSignOutDialog = false
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // White background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) Color.Red else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDestructive) Color.Red else Color.Black
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
