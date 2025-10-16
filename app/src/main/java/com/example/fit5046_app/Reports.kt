package com.example.fit5046_app

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fit5046_app.ui.theme.FIT5046_appTheme
import com.example.fit5046_app.components.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.fit5046_app.util.ReportDownloader
import com.example.fit5046_app.util.EmailShareDialog


@Preview(showBackground = true)
@Composable
fun ReportsPreview() {
    FIT5046_appTheme {
        Reports()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Reports(navController: androidx.navigation.NavController? = null) {
    var showDateRangePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val diaryViewModel: DiaryViewModel = viewModel()
    val reportsViewModel: ReportsViewModel = viewModel()
    val logs = diaryViewModel.getAllLogs().collectAsState(initial = emptyList())
    val reportsData by reportsViewModel.reportsData.collectAsState()


    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            bottomBar = {
                BottomButtonsContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    onDownloadClick = { ReportDownloader.saveReportAsPdf(context, logs.value) },
                    onShareClick = {val file = ReportDownloader.saveReportAsPdf(context, logs.value)
                        if (file != null) {
                            EmailShareDialog.shareReport(context, file)
                        } }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDateRangePicker = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        painterResource(id = R.drawable.date_range_24px),
                        contentDescription = "Select Date Range"
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End

        ) { _ ->
            Box() {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Glucose Analysis Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Glucose Spike Analysis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Identify which foods cause glucose spikes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Button(
                                onClick = { navController?.navigate(SubRoutes.GLUCOSE_ANALYSIS) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Analyze")
                            }
                        }
                    }
                    
                    // Health Correlation Overview Chart
                    HealthCorrelationCard(reportsData.glucoseAnalysis, reportsData.dietAnalysis, reportsData.exerciseAnalysis)
                    
                    GlucoseChartCard(reportsData.glucoseAnalysis)
                    DietChartCard(reportsData.dietAnalysis)
                    ExerciseChartCard(reportsData.exerciseAnalysis)
                }
            }
        }

        if (showDateRangePicker) {
            val dateRangeState = androidx.compose.material3.rememberDateRangePickerState()
            val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
            val headlineText = when {
                dateRangeState.selectedStartDateMillis != null && dateRangeState.selectedEndDateMillis != null ->
                    dateFormat.format(Date(dateRangeState.selectedStartDateMillis!!)) +
                            " - " +
                            dateFormat.format(Date(dateRangeState.selectedEndDateMillis!!))

                dateRangeState.selectedStartDateMillis != null ->
                    dateFormat.format(Date(dateRangeState.selectedStartDateMillis!!)) + " - "

                else -> "Select Date Range"
            }
            DatePickerDialog(
                onDismissRequest = { showDateRangePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDateRangePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDateRangePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DateRangePicker(
                    state = dateRangeState,
                    title = { Text(text = "") },
                    headline = {
                        Text(
                            text = headlineText,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

    }
}

@Composable
fun BottomButtonsContainer(
    modifier: Modifier = Modifier,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onDownloadClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painterResource(id = R.drawable.download_24px),
                contentDescription = "Download Icon"
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Download")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = onShareClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(painterResource(id = R.drawable.mail_24px), contentDescription = "Share Icon")
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Share")
        }
    }
}

@Composable
fun HealthCorrelationCard(
    glucoseAnalysis: GlucoseAnalysis,
    dietAnalysis: DietAnalysis,
    exerciseAnalysis: ExerciseAnalysis
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            Modifier
                .padding(18.dp)
        ) {
            Row {
                Icon(
                    painter = painterResource(id = R.drawable.reports_24px),
                    contentDescription = "Health Correlation Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .align(Alignment.CenterVertically)
                )
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Health Correlation Analysis",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "7-Day Overview: Glucose, Diet & Exercise Patterns",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )

            // Correlation Chart
            HealthCorrelationChart(
                glucoseAnalysis = glucoseAnalysis,
                dietAnalysis = dietAnalysis,
                exerciseAnalysis = exerciseAnalysis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun GlucoseChartCard(glucoseAnalysis: GlucoseAnalysis) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .padding(18.dp)
        ) {
            Row {
                Icon(
                    painter = painterResource(id = R.drawable.water_drop_24px),
                    contentDescription = "Blood Glucose Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .align(Alignment.CenterVertically)
                )
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Blood Glucose",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Average Glucose
                    Text(
                        text = "Past 7 Days - All Periods",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (glucoseAnalysis.averageGlucose > 0) 
                            String.format("%.1f mmol/L (avg.)", glucoseAnalysis.averageGlucose)
                        else "No data available",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Stats Table and Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Table
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(end = 8.dp)
                ) {
                    GlucoseStatRow(label = "Good", count = glucoseAnalysis.goodReadings)
                    GlucoseStatRow(label = "High", count = glucoseAnalysis.highReadings)
                    GlucoseStatRow(label = "Low", count = glucoseAnalysis.lowReadings)
                    GlucoseStatRow(label = "Total", count = glucoseAnalysis.totalReadings)
                }

                // Right Side: Chart
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlucoseTrendChart(glucoseAnalysis.dailyAverages)
                }
            }
        }

    }
}

@Composable
fun GlucoseStatRow(label: String, count: Int) {

    val countColor = when (label) {
        "High" -> Color(0xffff9713) // orange for high
        "Low" -> Color(0xFF8f84d4) // purple for low
        "Good" -> MaterialTheme.colorScheme.primary
        else -> Color.Black
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp,
            textAlign = TextAlign.Start
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            textAlign = TextAlign.End,
            color = countColor
        )
    }
}

@Composable
fun DietChartCard(dietAnalysis: DietAnalysis) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .padding(18.dp)
        ) {
            Row {
                Icon(
                    painterResource(id = R.drawable.diet_24px),
                    contentDescription = "Blood Glucose Icon",
                    Modifier
                        .padding(horizontal = 4.dp)
                        .align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Diet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Average Daily Calories
                    Text(
                        text = "Past 7 Days - All Periods",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (dietAnalysis.averageDailyCalories > 0) 
                            "${dietAnalysis.averageDailyCalories} cal/day (avg.)"
                        else "No data available",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))


            // Stats Table and Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Table
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                ) {
                    Text(
                        "Average Daily Intake",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    DietStatRow(label = "Carbs (g)", count = dietAnalysis.averageCarbs.toInt())
                    DietStatRow(label = "Protein (g)", count = dietAnalysis.averageProtein.toInt())
                    DietStatRow(label = "Fats (g)", count = dietAnalysis.averageFats.toInt())
                }

                // Right Side: Chart
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DietCaloriesChart(dietAnalysis.dailyCalories)
                }
            }
        }

    }
}

@Composable
fun DietStatRow(label: String, count: Int) {

    val countColor = when (label) {
        "Carbs (g)" -> Color(0xffff9713)
        "Fats (g)" -> Color(0xFF8f84d4)
        "Protein (g)" -> MaterialTheme.colorScheme.primary
        else -> Color.Black
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(vertical = 5.dp)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            textAlign = TextAlign.End,
            color = countColor
        )
    }
}

@Composable
fun ExerciseChartCard(exerciseAnalysis: ExerciseAnalysis) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .padding(18.dp)
        ) {
            Row {
                Icon(
                    painterResource(id = R.drawable.exercise_24px),
                    contentDescription = "Blood Glucose Icon",
                    Modifier
                        .padding(horizontal = 4.dp)
                        .align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Exercise",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Total Minutes
                    Text(
                        text = "Past 7 Days - All Periods",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (exerciseAnalysis.totalMinutes > 0) 
                            "${exerciseAnalysis.totalMinutes} min (total)"
                        else "No data available",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Daily exercise minutes chart
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExerciseMinutesChart(exerciseAnalysis.dailyMinutes)
                }
                
                // Exercise types pie chart
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExerciseTypesChart(exerciseAnalysis.exerciseTypes)
                }
            }

        }

    }
}