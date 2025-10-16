package com.example.fit5046_app

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_app.components.*
import com.example.fit5046_app.model.MedicationReminder
import com.example.fit5046_app.model.ReminderDayOfWeek
import com.example.fit5046_app.ui.theme.FIT5046_appTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

@Composable
fun Dashboard(
    onNavigateToMedicationReminders: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { DashboardViewModel(context.applicationContext as android.app.Application) }
    val dashboardData by viewModel.dashboardData.collectAsState()
    
    var nextReminder by remember { mutableStateOf<MedicationReminder?>(null) }
    var reminderText by remember { mutableStateOf("No upcoming reminders") }

    // Load reminders and calculate next one
    LaunchedEffect(Unit) {
        loadRemindersFromStorage(context) { reminders ->
            val activeReminders = reminders.filter { it.isActive }
            if (activeReminders.isNotEmpty()) {
                val next = findNextReminder(activeReminders)
                nextReminder = next
                if (next != null) {
                    val timeStr = String.format("%02d:%02d", next.hour, next.minute)
                    reminderText = "Take ${next.medicationName}${if (next.dosage.isNotEmpty()) " (${next.dosage})" else ""} at $timeStr"
                } else {
                    reminderText = "No upcoming reminders today"
                }
            } else {
                reminderText = "No medication reminders set"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onNavigateToMedicationReminders() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💊",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Next Medication Reminder",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = reminderText,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Manage Reminders",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.primary)
            }

            Text(
                text = "Today",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.DateRange, contentDescription = "Calendar",
                        tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Glucose Target",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = {}, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More",
                            tint = Color.Gray)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricCard("Insulin", "${dashboardData.insulinDosage}u", "💉")
                            Text(
                                text = "${dashboardData.latestGlucose?.let { String.format("%.1f", it) } ?: "N/A"}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard("Breakfast", "${dashboardData.mealCalories.breakfast}cal", "🍳")
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MetricCard("Readings", "${dashboardData.glucoseReadings}", "📊")

                            // Central glucose reading
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = RoundedCornerShape(60.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dashboardData.latestGlucose?.let { String.format("%.1f", it) } ?: "N/A",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "mmol/L",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            MetricCard("Lunch", "${dashboardData.mealCalories.lunch}cal", "🥗")
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricCard("Water", "${dashboardData.waterIntake}ml", "💧")
                            Text(
                                text = "Target: 7.0",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard("Dinner", "${dashboardData.mealCalories.dinner}cal", "🍽️")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📈", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "My Analysis: ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "on target",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Glucose Trend Chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Blood Glucose Trend",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        GlucoseTrendChart(glucoseTrend = dashboardData.glucoseTrend)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Weekly Glucose Chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Weekly Glucose Average",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        WeeklyGlucoseChart(weeklyTrend = dashboardData.weeklyTrend)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Meal Calories Chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Today's Meal Calories",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MealCaloriesChart(mealCalories = dashboardData.mealCalories)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        icon = "🩺",
                        title = "Blood Sugar",
                        subtitle = "Last reading: Today",
                        value = dashboardData.latestGlucose?.let { String.format("%.1f mmol/L", it) } ?: "No data",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        icon = "🔥",
                        title = "Today's Calories",
                        subtitle = "Total intake",
                        value = "${dashboardData.todayCalories} cal",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        icon = "💧",
                        title = "Water Intake",
                        subtitle = "Today's consumption",
                        value = "${dashboardData.waterIntake} ml",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        icon = "💉",
                        title = "Insulin",
                        subtitle = "Today's dosage",
                        value = "${dashboardData.insulinDosage}u",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun MetricCard(title: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = icon, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SummaryCard(
    icon: String,
    title: String,
    subtitle: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                if (value.isNotEmpty()) {
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Helper functions for medication reminders
private fun loadRemindersFromStorage(
    context: Context,
    onResult: (List<MedicationReminder>) -> Unit
) {
    try {
        val sharedPrefs = context.getSharedPreferences("medication_reminders", Context.MODE_PRIVATE)
        val remindersJson = sharedPrefs.getString("reminders", "[]")

        if (remindersJson.isNullOrEmpty() || remindersJson == "[]") {
            onResult(emptyList())
            return
        }

        val gson = Gson()
        val type = object : TypeToken<List<MedicationReminder>>() {}.type
        val reminders = gson.fromJson<List<MedicationReminder>>(remindersJson, type) ?: emptyList()
        onResult(reminders)
    } catch (e: Exception) {
        onResult(emptyList())
    }
}

private fun findNextReminder(reminders: List<MedicationReminder>): MedicationReminder? {
    val now = Calendar.getInstance()
    val currentHour = now.get(Calendar.HOUR_OF_DAY)
    val currentMinute = now.get(Calendar.MINUTE)
    val currentDayOfWeek = getCurrentDayOfWeek(now.get(Calendar.DAY_OF_WEEK))

    // Filter reminders for today
    val todayReminders = reminders.filter { reminder ->
        reminder.daysOfWeek.contains(currentDayOfWeek)
    }

    if (todayReminders.isEmpty()) {
        return null
    }

    // Find the next reminder today
    val upcomingToday = todayReminders.filter { reminder ->
        val reminderMinutes = reminder.hour * 60 + reminder.minute
        val currentMinutes = currentHour * 60 + currentMinute
        reminderMinutes > currentMinutes
    }

    return if (upcomingToday.isNotEmpty()) {
        // Return the earliest upcoming reminder today
        upcomingToday.minByOrNull { it.hour * 60 + it.minute }
    } else {
        // No more reminders today, return the earliest one for today
        todayReminders.minByOrNull { it.hour * 60 + it.minute }
    }
}

private fun getCurrentDayOfWeek(calendarDayOfWeek: Int): ReminderDayOfWeek {
    return when (calendarDayOfWeek) {
        Calendar.SUNDAY -> ReminderDayOfWeek.SUNDAY
        Calendar.MONDAY -> ReminderDayOfWeek.MONDAY
        Calendar.TUESDAY -> ReminderDayOfWeek.TUESDAY
        Calendar.WEDNESDAY -> ReminderDayOfWeek.WEDNESDAY
        Calendar.THURSDAY -> ReminderDayOfWeek.THURSDAY
        Calendar.FRIDAY -> ReminderDayOfWeek.FRIDAY
        Calendar.SATURDAY -> ReminderDayOfWeek.SATURDAY
        else -> ReminderDayOfWeek.MONDAY
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    FIT5046_appTheme { Dashboard() }
}
