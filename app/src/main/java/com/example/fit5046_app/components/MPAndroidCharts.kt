package com.example.fit5046_app.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.example.fit5046_app.model.ExerciseType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HealthCorrelationMPChart(
    glucoseAnalysis: com.example.fit5046_app.GlucoseAnalysis,
    dietAnalysis: com.example.fit5046_app.DietAnalysis,
    exerciseAnalysis: com.example.fit5046_app.ExerciseAnalysis,
    dateRange: Pair<LocalDate, LocalDate>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val dailyData = glucoseAnalysis.dailyAverages.map { glucoseData ->
        val date = glucoseData.date
        val glucose = glucoseData.average ?: 0f
        val calories = dietAnalysis.dailyCalories.find { it.date == date }?.calories ?: 0
        val exercise = exerciseAnalysis.dailyMinutes.find { it.date == date }?.minutes ?: 0
        Triple(date, glucose, Pair(calories, exercise))
    }
    
    if (dailyData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "No health data available for correlation analysis",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }
    
    Column(modifier = modifier) {
        Text(
            text = "Health Correlation Analysis",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            factory = { context ->
                CombinedChart(context).apply {
                    val glucoseEntries = dailyData.mapIndexed { index, (_, glucose, _) ->
                        Entry(index.toFloat(), glucose)
                    }
                    val glucoseDataSet = LineDataSet(glucoseEntries, "Glucose (mmol/L)").apply {
                        color = 0xFF2196F3.toInt()
                        setCircleColor(0xFF2196F3.toInt())
                        lineWidth = 3f
                        circleRadius = 4f
                        setDrawValues(false)
                    }
                    
                    val caloriesEntries = dailyData.mapIndexed { index, (_, _, data) ->
                        BarEntry(index.toFloat(), data.first.toFloat())
                    }
                    val caloriesDataSet = BarDataSet(caloriesEntries, "Calories").apply {
                        color = 0xFF4CAF50.toInt()
                        setDrawValues(false)
                    }
                    
                    val exerciseEntries = dailyData.mapIndexed { index, (_, _, data) ->
                        Entry(index.toFloat(), data.second.toFloat())
                    }
                    val exerciseDataSet = ScatterDataSet(exerciseEntries, "Exercise (min)").apply {
                        color = 0xFFFF9800.toInt()
                        scatterShapeSize = 8f
                        setDrawValues(false)
                    }
                    
                    val combinedData = CombinedData().apply {
                        setData(LineData(glucoseDataSet))
                        setData(BarData(caloriesDataSet))
                        setData(ScatterData(exerciseDataSet))
                    }
                    
                    data = combinedData
                    description.isEnabled = false
                    legend.isEnabled = true
                    legend.textSize = 12f
                    
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.setDrawGridLines(false)
                    xAxis.granularity = 1f
                    xAxis.labelRotationAngle = -45f
                    xAxis.textSize = 10f
                    xAxis.valueFormatter = object : IndexAxisValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index < dailyData.size) {
                                dailyData[index].first.format(DateTimeFormatter.ofPattern("MM/dd"))
                            } else ""
                        }
                    }
                    
                    axisLeft.setDrawGridLines(true)
                    axisLeft.textSize = 10f
                    axisRight.isEnabled = false
                    
                    animateY(2000)
                    invalidate()
                }
            }
        )
    }
}

@Composable
fun GlucoseTrendMPChart(
    dailyAverages: List<com.example.fit5046_app.DailyGlucoseAverage>,
    dateRange: Pair<LocalDate, LocalDate>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    if (dailyAverages.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "No glucose data available",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            LineChart(context).apply {
                val entries = dailyAverages.mapIndexed { index, data ->
                    Entry(index.toFloat(), data.average ?: 0f)
                }
                
                val dataSet = LineDataSet(entries, "Glucose (mmol/L)").apply {
                    color = 0xFF2196F3.toInt()
                    setCircleColor(0xFF2196F3.toInt())
                    lineWidth = 3f
                    circleRadius = 4f
                    setDrawValues(false)
                    setDrawFilled(true)
                    fillColor = 0xFF2196F3.toInt()
                    fillAlpha = 50
                }
                
                data = LineData(dataSet)
                description.isEnabled = false
                legend.isEnabled = false
                
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.labelRotationAngle = -45f
                xAxis.textSize = 10f
                xAxis.valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index < dailyAverages.size) {
                            dailyAverages[index].date.format(DateTimeFormatter.ofPattern("MM/dd"))
                        } else ""
                    }
                }
                
                axisLeft.setDrawGridLines(true)
                axisLeft.textSize = 10f
                axisRight.isEnabled = false
                
                val limitLine = LimitLine(7f, "Target Zone")
                limitLine.lineColor = 0xFF4CAF50.toInt()
                limitLine.lineWidth = 2f
                axisLeft.addLimitLine(limitLine)
                
                animateY(2000)
                invalidate()
            }
        }
    )
}

@Composable
fun DietCaloriesMPChart(
    dailyCalories: List<com.example.fit5046_app.DailyCalories>,
    dateRange: Pair<LocalDate, LocalDate>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    if (dailyCalories.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "No diet data available",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            BarChart(context).apply {
                val entries = dailyCalories.mapIndexed { index, data ->
                    BarEntry(index.toFloat(), data.calories.toFloat())
                }
                
                val dataSet = BarDataSet(entries, "Calories").apply {
                    color = 0xFF4CAF50.toInt()
                    setDrawValues(false)
                }
                
                data = BarData(dataSet)
                description.isEnabled = false
                legend.isEnabled = false
                setFitBars(true)
                
                // X-axis setup
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.labelRotationAngle = -45f
                xAxis.textSize = 10f
                xAxis.valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index < dailyCalories.size) {
                            dailyCalories[index].date.format(DateTimeFormatter.ofPattern("MM/dd"))
                        } else ""
                    }
                }
                
                // Y-axis setup
                axisLeft.setDrawGridLines(true)
                axisLeft.textSize = 10f
                axisRight.isEnabled = false
                
                animateY(2000)
                invalidate()
            }
        }
    )
}

@Composable
fun ExerciseMinutesMPChart(
    dailyMinutes: List<com.example.fit5046_app.DailyExercise>,
    dateRange: Pair<LocalDate, LocalDate>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    if (dailyMinutes.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "No exercise data available",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            BarChart(context).apply {
                val entries = dailyMinutes.mapIndexed { index, data ->
                    BarEntry(index.toFloat(), data.minutes.toFloat())
                }
                
                val dataSet = BarDataSet(entries, "Exercise (min)").apply {
                    color = 0xFFFF9800.toInt()
                    setDrawValues(false)
                }
                
                data = BarData(dataSet)
                description.isEnabled = false
                legend.isEnabled = false
                setFitBars(true)
                
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.labelRotationAngle = -45f
                xAxis.textSize = 10f
                xAxis.valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index < dailyMinutes.size) {
                            dailyMinutes[index].date.format(DateTimeFormatter.ofPattern("MM/dd"))
                        } else ""
                    }
                }
                
                axisLeft.setDrawGridLines(true)
                axisLeft.textSize = 10f
                axisRight.isEnabled = false
                
                animateY(2000)
                invalidate()
            }
        }
    )
}

@Composable
fun ExerciseTypesMPChart(
    exerciseTypes: Map<ExerciseType, Int>,
    dateRange: Pair<LocalDate, LocalDate>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    if (exerciseTypes.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "No exercise type data available",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            PieChart(context).apply {
                val entries = exerciseTypes.map { (type, minutes) ->
                    PieEntry(minutes.toFloat(), type.displayName)
                }
                
                val dataSet = PieDataSet(entries, "Exercise Types").apply {
                    colors = ColorTemplate.COLORFUL_COLORS.toList()
                    setDrawValues(true)
                    valueTextSize = 12f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()} min"
                        }
                    }
                }
                
                data = PieData(dataSet)
                description.isEnabled = false
                legend.isEnabled = true
                legend.textSize = 10f
                setDrawEntryLabels(true)
                setEntryLabelTextSize(10f)
                
                animateY(2000)
                invalidate()
            }
        }
    )
}
