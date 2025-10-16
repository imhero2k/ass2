package com.example.fit5046_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fit5046_app.model.EntryPeriod
import com.example.fit5046_app.model.GlucoseRanges
import com.example.fit5046_app.model.Log
import com.example.fit5046_app.model.LogDisplayItem
import com.example.fit5046_app.model.LogType
import com.example.fit5046_app.ui.theme.FIT5046_appTheme
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale
import android.util.Log as AndroidLog

object DateTimeFormatters {
    val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) // e.g., Sep 9, 2025
    val dayOfWeekFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE") // e.g., Tuesday
    val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) // e.g., 8:30 AM
}

data class LogFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val logTypes: Set<LogType> = emptySet(),
    val entryPeriods: Set<EntryPeriod> = emptySet(),
    val includeHighGlucose: Boolean = false,
    val includeLowGlucose: Boolean = false,
    val includeNormalGlucose: Boolean = false
) {
    fun isActive(): Boolean {
        return startDate != null || endDate != null ||
                logTypes.isNotEmpty() || entryPeriods.isNotEmpty() ||
                includeHighGlucose || includeLowGlucose || includeNormalGlucose
    }

    fun clear(): LogFilter {
        return LogFilter()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Diary(navController: androidx.navigation.NavController? = null) {
    val context = LocalContext.current
    val diaryViewModel: DiaryViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    
    // Get current user ID from Firebase Auth
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
    
    // Get logs only for the current user
    val allLogs by diaryViewModel.getAllLogsForUser(currentUserId).collectAsState(initial = emptyList())
    allLogs.forEach { AndroidLog.d("DEBUG_DIET", it.toString()) }

    if (allLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No logs yet. Add some!", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    var searchInput by rememberSaveable { mutableStateOf("") }
    var showFilter by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(LogFilter()) }

    // Step 1: Determine valid dates based on glucose filter 
    val validDates: Set<LocalDate>? = if (currentFilter.includeHighGlucose || currentFilter.includeLowGlucose || currentFilter.includeNormalGlucose) {
        // Find all days that have glucose readings matching the criteria
        allLogs
            .filter { it.type == LogType.BLOOD_GLUCOSE }
            .map { it as LogDisplayItem.GlucoseItem }
            .filter { glucoseLog ->
                val isHigh = glucoseLog.value > GlucoseRanges.GLUCOSE_UPPER_NORMAL
                val isLow = glucoseLog.value < GlucoseRanges.GLUCOSE_LOWER_NORMAL
                val isNormal = glucoseLog.value in GlucoseRanges.GLUCOSE_LOWER_NORMAL..GlucoseRanges.GLUCOSE_UPPER_NORMAL
                
                (isHigh && currentFilter.includeHighGlucose) ||
                (isLow && currentFilter.includeLowGlucose) ||
                (isNormal && currentFilter.includeNormalGlucose)
            }
            .map { it.dateTime.toLocalDate() }
            .toSet()
    } else {
        null // No glucose filter active
    }

    // Step 2: Apply all filters (date range, glucose dates, log type, entry period)
    val filteredLogsBeforeSearch = allLogs.filter { log ->
        // Date range filter
        if (currentFilter.startDate != null || currentFilter.endDate != null) {
            val logDate = log.dateTime.toLocalDate()
            if (currentFilter.startDate != null && logDate < currentFilter.startDate) return@filter false
            if (currentFilter.endDate != null && logDate > currentFilter.endDate) return@filter false
        }

        // Dates by glucose level filter
        if (validDates != null && log.dateTime.toLocalDate() !in validDates) {
            return@filter false
        }

        // Log type filter
        if (currentFilter.logTypes.isNotEmpty() && log.type !in currentFilter.logTypes) {
            return@filter false
        }

        // Entry period filter
        if (currentFilter.entryPeriods.isNotEmpty() && log.period !in currentFilter.entryPeriods) {
            return@filter false
        }

        true
    }

    // Step 3: Apply text search 
    val filteredLogs = if (searchInput.isBlank()) {
        filteredLogsBeforeSearch
    } else {
        filteredLogsBeforeSearch.filter { log ->
            (log.notes?.contains(searchInput, ignoreCase = true) == true) ||
                    when (log.type) {
                        LogType.DIET -> (log as LogDisplayItem.DietItem).foodItems != null &&
                                log.foodItems!!.any {
                                    it.contains(
                                        searchInput,
                                        ignoreCase = true
                                    )
                                }

                        LogType.MEDICATION -> (log as LogDisplayItem.MedicationItem).name.contains(
                            searchInput,
                            ignoreCase = true
                        )

                        LogType.EXERCISE -> (log as LogDisplayItem.ExerciseItem).exerciseType.displayName.contains(
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
                        navController?.navigate("log")
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
                            .height(56.dp)
                            .weight(1f),
                        placeholder = {
                            Text(
                                "Search note keywords",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(id = R.drawable.search_24px),
                                contentDescription = "Search Icon",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    // Filter Button
                    TextButton(
                        contentPadding = PaddingValues(5.dp),
                        onClick = {
                            showFilter = true
                        }) {
                        Icon(
                            painterResource(id = R.drawable.filter_24px),
                            contentDescription = "Filter Logs",
                            modifier = Modifier.size(20.dp),
                            tint = if (currentFilter.isActive()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "Filter",
                            fontSize = 16.sp,
                            color = if (currentFilter.isActive()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Filter dialog
                if (showFilter) {
                    FilterDialog(
                        currentFilter = currentFilter,
                        onDismiss = { showFilter = false },
                        onApply = { newFilter ->
                            currentFilter = newFilter
                            showFilter = false
                        },
                        onClear = {
                            currentFilter = LogFilter()
                            showFilter = false
                        }
                    )
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
                                dailyLogs = logs.sortedByDescending { it.dateTime },
                                navController = navController
                            )
                        }
                    }
                }

            }
        }
    }


}

@Composable
fun SingleDayLogCard(
    dailyLogs: List<LogDisplayItem>, 
    modifier: Modifier = Modifier,
    navController: androidx.navigation.NavController? = null
) {
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
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            dailyLogs.forEachIndexed { index, logEntry ->
                SingleLogEntryRow(
                    log = logEntry,
                    navController = navController
                )
                if (index < dailyLogs.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                }
            }
        }
    }

}

@Composable
fun SingleLogEntryRow(
    log: LogDisplayItem, 
    modifier: Modifier = Modifier,
    navController: androidx.navigation.NavController? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = log.type == LogType.DIET || 
                         log.type == LogType.BLOOD_GLUCOSE || 
                         log.type == LogType.EXERCISE ||
                         log.type == LogType.MEDICATION
            ) {
                // Navigate to Log screen with log ID and type
                navController?.navigate("log?logId=${log.id}&logType=${log.type.name}") {
                    launchSingleTop = true
                }
            },
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
            when (log.type) {
                LogType.DIET -> DietLogContent(log as LogDisplayItem.DietItem)
                LogType.BLOOD_GLUCOSE -> BloodGlucoseLogContent(log as LogDisplayItem.GlucoseItem)
                LogType.EXERCISE -> ExerciseLogContent(log as LogDisplayItem.ExerciseItem)
                LogType.MEDICATION -> MedicationLogContent(log as LogDisplayItem.MedicationItem)
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
    }
}


@Composable
fun DietLogContent(log: LogDisplayItem.DietItem) {
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
        if (log.foodItems != null && log.foodItems.isNotEmpty()) {
            Text(
                text = log.foodItems.joinToString { it },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}


@Composable
fun BloodGlucoseLogContent(log: LogDisplayItem.GlucoseItem) {
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
fun ExerciseLogContent(log: LogDisplayItem.ExerciseItem) {
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
fun MedicationLogContent(log: LogDisplayItem.MedicationItem) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentFilter: LogFilter,
    onDismiss: () -> Unit,
    onApply: (LogFilter) -> Unit,
    onClear: () -> Unit
) {
    var startDate by remember { mutableStateOf(currentFilter.startDate) }
    var endDate by remember { mutableStateOf(currentFilter.endDate) }
    var selectedLogTypes by remember { mutableStateOf(currentFilter.logTypes) }
    var selectedPeriods by remember { mutableStateOf(currentFilter.entryPeriods) }
    var includeHigh by remember { mutableStateOf(currentFilter.includeHighGlucose) }
    var includeLow by remember { mutableStateOf(currentFilter.includeLowGlucose) }
    var includeNormal by remember { mutableStateOf(currentFilter.includeNormalGlucose) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filter Logs", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Date Range Section
                Text(
                    "Date Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            startDate != null && endDate != null -> "${startDate!!.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))} - ${endDate!!.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
                            startDate != null -> "From ${startDate!!.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
                            endDate != null -> "Until ${endDate!!.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
                            else -> "Select Date Range"
                        }
                    )
                }
                
                if (startDate != null || endDate != null) {
                    TextButton(
                        onClick = {
                            startDate = null
                            endDate = null
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear Dates")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Glucose Level Section
                Text(
                    "Days with Glucose Levels",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Filter for days that have at least one glucose reading with:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeHigh = !includeHigh }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeHigh,
                        onCheckedChange = { includeHigh = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("High Glucose (>${GlucoseRanges.GLUCOSE_UPPER_NORMAL} mmol/L)")
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeLow = !includeLow }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeLow,
                        onCheckedChange = { includeLow = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Low Glucose (<${GlucoseRanges.GLUCOSE_LOWER_NORMAL} mmol/L)")
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeNormal = !includeNormal }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeNormal,
                        onCheckedChange = { includeNormal = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Normal Glucose (${GlucoseRanges.GLUCOSE_LOWER_NORMAL}-${GlucoseRanges.GLUCOSE_UPPER_NORMAL} mmol/L)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Log Type Section
                Text(
                    "Log Types",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LogType.entries.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { logType ->
                            FilterChip(
                                selected = logType in selectedLogTypes,
                                onClick = {
                                    selectedLogTypes = if (logType in selectedLogTypes) {
                                        selectedLogTypes - logType
                                    } else {
                                        selectedLogTypes + logType
                                    }
                                },
                                label = { Text(logType.displayName, fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Entry Period Section
                Text(
                    "Entry Periods",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Group periods into categories
                val mealPeriods = listOf(
                    EntryPeriod.BREAKFAST, EntryPeriod.LUNCH, 
                    EntryPeriod.DINNER, EntryPeriod.SNACK
                )
                val beforeAfterPeriods = listOf(
                    EntryPeriod.BEFORE_BREAKFAST, EntryPeriod.AFTER_BREAKFAST,
                    EntryPeriod.BEFORE_LUNCH, EntryPeriod.AFTER_LUNCH,
                    EntryPeriod.BEFORE_DINNER, EntryPeriod.AFTER_DINNER,
                    EntryPeriod.BEFORE_SNACK, EntryPeriod.AFTER_SNACK
                )
                val timePeriods = listOf(
                    EntryPeriod.MORNING, EntryPeriod.AFTERNOON,
                    EntryPeriod.EVENING, EntryPeriod.NIGHT
                )
                val activityPeriods = listOf(
                    EntryPeriod.AFTER_EXERCISE, EntryPeriod.AFTER_MEDICATION
                )

                Column {
                    Text("Meal Periods:", style = MaterialTheme.typography.labelMedium)
                    mealPeriods.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            row.forEach { period ->
                                FilterChip(
                                    selected = period in selectedPeriods,
                                    onClick = {
                                        selectedPeriods = if (period in selectedPeriods) {
                                            selectedPeriods - period
                                        } else {
                                            selectedPeriods + period
                                        }
                                    },
                                    label = { Text(period.displayName, fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Before/After Meals:", style = MaterialTheme.typography.labelMedium)
                    beforeAfterPeriods.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            row.forEach { period ->
                                FilterChip(
                                    selected = period in selectedPeriods,
                                    onClick = {
                                        selectedPeriods = if (period in selectedPeriods) {
                                            selectedPeriods - period
                                        } else {
                                            selectedPeriods + period
                                        }
                                    },
                                    label = { Text(period.displayName, fontSize = 11.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Time Periods:", style = MaterialTheme.typography.labelMedium)
                    timePeriods.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            row.forEach { period ->
                                FilterChip(
                                    selected = period in selectedPeriods,
                                    onClick = {
                                        selectedPeriods = if (period in selectedPeriods) {
                                            selectedPeriods - period
                                        } else {
                                            selectedPeriods + period
                                        }
                                    },
                                    label = { Text(period.displayName, fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Activity Periods:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        activityPeriods.forEach { period ->
                            FilterChip(
                                selected = period in selectedPeriods,
                                onClick = {
                                    selectedPeriods = if (period in selectedPeriods) {
                                        selectedPeriods - period
                                    } else {
                                        selectedPeriods + period
                                    }
                                },
                                label = { Text(period.displayName, fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onClear) {
                    Text("Clear All")
                }
                Row {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onApply(
                                LogFilter(
                                    startDate = startDate,
                                    endDate = endDate,
                                    logTypes = selectedLogTypes,
                                    entryPeriods = selectedPeriods,
                                    includeHighGlucose = includeHigh,
                                    includeLowGlucose = includeLow,
                                    includeNormalGlucose = includeNormal
                                )
                            )
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        },
        dismissButton = {}
    )

    // Date Range Picker Dialog
    if (showDatePicker) {
        val dateRangeState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            initialSelectedEndDateMillis = endDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateRangeState.selectedStartDateMillis?.let { startMillis ->
                            startDate = Instant.ofEpochMilli(startMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        dateRangeState.selectedEndDateMillis?.let { endMillis ->
                            endDate = Instant.ofEpochMilli(endMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangeState,
                title = { Text(text = "Select Date Range", modifier = Modifier.padding(16.dp)) },
                headline = {
                    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
                    val headlineText = when {
                        dateRangeState.selectedStartDateMillis != null && dateRangeState.selectedEndDateMillis != null ->
                            dateFormat.format(Date(dateRangeState.selectedStartDateMillis!!)) + " - " +
                            dateFormat.format(Date(dateRangeState.selectedEndDateMillis!!))
                        dateRangeState.selectedStartDateMillis != null ->
                            dateFormat.format(Date(dateRangeState.selectedStartDateMillis!!)) + " - "
                        else -> "Select Date Range"
                    }
                    Text(
                        text = headlineText,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}



