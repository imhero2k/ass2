package com.example.fit5046_app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fit5046_app.ui.theme.FIT5046_appTheme

@Composable
fun UserSetting() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SettingItem(
                title = "Edit Profile",
                subtitle = "Update your personal information",
                icon = Icons.Default.Person,
                onClick = { /* Navigate to Edit Profile */ }
            )
        }

        item {
            SettingItem(
                title = "Reminder Settings",
                subtitle = "Manage medication reminders",
                icon = Icons.Default.Notifications,
                onClick = { /* Navigate to Reminder Settings */ }
            )
        }

        item {
            SettingItem(
                title = "About",
                subtitle = "App information and disclaimer",
                icon = Icons.Default.Info,
                onClick = { /* Navigate to About */ }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SettingItem(
                title = "Log Out",
                subtitle = "Sign out of your account",
                icon = Icons.Default.ExitToApp,
                onClick = { /* Log out action */ },
                isDestructive = true
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isDestructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserSettingPreview() {
    FIT5046_appTheme {
        UserSetting()
    }
}