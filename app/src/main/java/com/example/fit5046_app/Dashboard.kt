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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_app.ui.theme.FIT5046_appTheme


@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    FIT5046_appTheme {
        Reports()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Reports() {
    var showDateRangePicker by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            bottomBar = {
                BottomButtonsContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    onDownloadClick = { /* TODO: Implement Download */ },
                    onShareClick = { /* TODO: Implement Share */ }
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
                    GlucoseChartCard()
                    DietChartCard()
                    ExerciseChartCard()
                }
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
fun GlucoseChartCard() {
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
                    painterResource(id = R.drawable.water_drop_24px),
                    contentDescription = "Blood Glucose Icon",
                    Modifier
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
                    // TODO: Replace with average glucose from logs
                    Text(
                        text = "6.1 mmol/L (avg.)",
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
                    GlucoseStatRow(label = "Good", count = 4)
                    GlucoseStatRow(label = "High", count = 2)
                    GlucoseStatRow(label = "Low", count = 1)
                    GlucoseStatRow(label = "Total", count = 7)
                }

                // Right Side: Chart Image
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: Replace with actual chart
                    Image(
                        painter = painterResource(id = R.drawable.glucose_chart),
                        contentDescription = "Glucose Chart Placeholder",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                    )
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
fun DietChartCard() {
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
                        .align(Alignment.CenterVertically)
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

                    // TODO: Replace with average from logs
                    Text(
                        text = "2056 cal/day (avg.)",
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
                    DietStatRow(label = "Carbs (g)", count = 135)
                    DietStatRow(label = "Protein (g)", count = 104)
                    DietStatRow(label = "Fats (g)", count = 67)
                }

                // Right Side: Chart Image
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: Replace with actual chart
                    Image(
                        painter = painterResource(id = R.drawable.diet_chart),
                        contentDescription = "Diet Chart Placeholder",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                    )
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
fun ExerciseChartCard() {
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
                        .align(Alignment.CenterVertically)
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

                    // TODO: Replace with average glucose from logs
                    Text(
                        text = "175 min (total)",
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
                Image(
                    alignment = Alignment.CenterStart,
                    painter = painterResource(id = R.drawable.exercise_chart),
                    contentDescription = "Exercise Chart Placeholder",
                )
            }

        }

    }
}