package com.example.fit5046_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_app.model.BloodGlucoseLog
import com.example.fit5046_app.model.DietLog
import com.example.fit5046_app.model.ExerciseLog
import com.example.fit5046_app.model.Log
import com.example.fit5046_app.model.MedicationLog
import com.example.fit5046_app.ui.theme.FIT5046_appTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateTimeFormatters {
    val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) // e.g., Sep 9, 2025
    val dayOfWeekFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE") // e.g., Tuesday
    val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) // e.g., 8:30 AM
}


@Preview(showBackground = true)
@Composable
fun Preview() {
    FIT5046_appTheme {
        val myLogs = getMockLogs()
        val emptyLogs = listOf<Log>()
        Diary(myLogs)
    }
}


@Composable
fun Diary(allLogs: List<Log>) {
    if (allLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No logs yet. Add some!", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    var searchInput by rememberSaveable { mutableStateOf("") }
    var showFilter by remember { mutableStateOf(false) }

    // TODO: Refine search/filter logic
    val filteredLogs =
        if (searchInput.isBlank()) {
            allLogs
        } else {
            allLogs.filter { log ->
                // Text search in notes or specific fields
                (log.notes?.contains(searchInput, ignoreCase = true) == true) ||
                        when (log) {
                            is DietLog -> log.foodItems.any {
                                it.name.contains(
                                    searchInput,
                                    ignoreCase = true
                                )
                            }

                            is MedicationLog -> log.name.contains(searchInput, ignoreCase = true)
                            is ExerciseLog -> log.exerciseType.displayName.contains(
                                searchInput,
                                ignoreCase = true
                            )

                            else -> false
                        }
            }
        }

    // group logs by date
    val dailyLogs = filteredLogs.groupBy { it.dateTime.toLocalDate() }
        .toSortedMap(compareByDescending { it })


    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // TODO: Go to the Log page
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add new log")
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Search & Filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 10.dp,
                            bottom = 8.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .weight(1f),
                        placeholder = {
                            Text(
                                "Search note keywords",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(id = R.drawable.search_24px),
                                contentDescription = "Search Icon",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true
                    )

                    // Filter Button
                    TextButton(
                        contentPadding = PaddingValues(5.dp),
                        onClick = {
                            // TODO: Implement filter dialog
                            showFilter = true
                        }) {
                        Icon(
                            painterResource(id = R.drawable.filter_24px),
                            contentDescription = "Filter Logs",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Filter", fontSize = 16.sp)
                    }
                }

                // Daily Log Cards
                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    dailyLogs.forEach { (date, logs) ->
                        // Date Header
                        item {
                            Text(
                                text = "${date.format(DateTimeFormatters.dayOfWeekFormatter)}, ${
                                    date.format(
                                        DateTimeFormatters.dateFormatter
                                    )
                                }",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                        // Single Day Log Card
                        item {
                            SingleDayLogCard(
                                logs.sortedByDescending { it.dateTime }
                            )
                        }
                    }
                }

            }
        }
    }


}

@Composable
fun SingleDayLogCard(dailyLogs: List<Log>, modifier: Modifier = Modifier) {
    if (dailyLogs.isEmpty()) {
        // This case might not be reached if dailyLogs filters out empty dates
        Text(
            "No entries for this day.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.padding(vertical = 8.dp)
        )
        return
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            dailyLogs.forEachIndexed { index, logEntry ->
                SingleLogEntryRow(log = logEntry)
                if (index < dailyLogs.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                }
            }
        }
    }

}

@Composable
fun SingleLogEntryRow(log: Log, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Left Column for Time
        Text(
            text = log.dateTime.toLocalTime().format(DateTimeFormatters.timeFormatter),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .width(80.dp)
                .padding(
                    end = 10.dp,
                    top = 2.dp
                )
        )

        // Right Column for Log Details
        Column(modifier = Modifier.weight(1f)) {
            // Log Type and Period
            Text(
                text = "${log.type.displayName} - ${log.period.displayName}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Log Data
            when (log) {
                is DietLog -> DietLogContent(log)
                is BloodGlucoseLog -> BloodGlucoseLogContent(log)
                is ExerciseLog -> ExerciseLogContent(log)
                is MedicationLog -> MedicationLogContent(log)
            }

            // Log Notes if Available
            log.notes?.let {
                if (it.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Notes: ${it}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // TODO: Edit log via button or clicking the row?
//        Icon(
//            painter = painterResource(id = R.drawable.edit_24px),
//            contentDescription = "Edit log"
//        )
    }
}


@Composable
fun DietLogContent(log: DietLog) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) { append("${log.calories ?: "N/A"} kcal") }

            },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "P: ${log.proteinGrams?.format(1) ?: "N/A"}g, F: ${log.fatGrams?.format(1) ?: "N/A"}g, C: ${
                log.carbGrams?.format(
                    1
                ) ?: "N/A"
            }g",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (log.foodItems.isNotEmpty()) {
            Text(
                text = log.foodItems.joinToString { it.name },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

object GlucoseRanges {
    const val GLUCOSE_UPPER_NORMAL = 7.8f
    const val GLUCOSE_LOWER_NORMAL = 4.0f
}

@Composable
fun BloodGlucoseLogContent(log: BloodGlucoseLog) {
    val valueColor = when {
        log.value > GlucoseRanges.GLUCOSE_UPPER_NORMAL -> Color(0xffff9713) // orange for high
        log.value < GlucoseRanges.GLUCOSE_LOWER_NORMAL -> Color(0xFF8f84d4) // purple for low
        else -> MaterialTheme.colorScheme.primary // primary for normal
    }

    Text(
        buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = valueColor
                )
            ) {
                append("${log.value.format(1)} mmol/L")
            }
        }, style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun ExerciseLogContent(log: ExerciseLog) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            "${log.exerciseType.displayName} - ${log.durationMinutes} min",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${log.intensity.displayName} intensity",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

    }
}

@Composable
fun MedicationLogContent(log: MedicationLog) {
    Text(
        "${log.name} - ${log.dosage.format(0)} ${log.dosageUnit}",
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

fun Number.format(digits: Int): String {
    return when (this) {
        is Double -> "%.${digits}f".format(this)
        is Float -> "%.${digits}f".format(this)
        else -> this.toString()
    }
}





