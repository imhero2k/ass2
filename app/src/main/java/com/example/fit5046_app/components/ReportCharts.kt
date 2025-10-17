package com.example.fit5046_app.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

@Composable
fun GlucoseTrendChart(
    dailyAverages: List<com.example.fit5046_app.DailyGlucoseAverage>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val validData = dailyAverages.filter { it.average != null }
    
    if (validData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No glucose data available",
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        return
    }
    
    val maxValue = validData.maxOf { it.average!! }
    val minValue = validData.minOf { it.average!! }
    val range = maxValue - minValue
    val padding = range * 0.1f
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val chartHeight = canvasHeight - 20f
            val chartWidth = canvasWidth - 20f
            
            // Draw target range
            val targetMin = 4f
            val targetMax = 7f
            val targetMinY = 10f + chartHeight - ((targetMin - minValue + padding) / (range + 2 * padding)) * chartHeight
            val targetMaxY = 10f + chartHeight - ((targetMax - minValue + padding) / (range + 2 * padding)) * chartHeight
            
            // Draw target range background
            drawRect(
                color = colorScheme.primary.copy(alpha = 0.1f),
                topLeft = Offset(10f, targetMaxY),
                size = androidx.compose.ui.geometry.Size(chartWidth, targetMinY - targetMaxY)
            )
            
            // Draw glucose line
            if (dailyAverages.size > 1) {
                val points = dailyAverages.mapIndexed { index, daily ->
                    val x = 10f + (chartWidth / (dailyAverages.size - 1)) * index
                    val value = daily.average ?: 0f
                    val normalizedValue = (value - minValue + padding) / (range + 2 * padding)
                    val y = 10f + chartHeight - (normalizedValue * chartHeight)
                    Offset(x, y)
                }
                
                // Draw line
                for (i in 0 until points.size - 1) {
                    if (dailyAverages[i].average != null && dailyAverages[i + 1].average != null) {
                        drawLine(
                            color = colorScheme.primary,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
                
                // Draw points
                points.forEachIndexed { index, point ->
                    if (dailyAverages[index].average != null) {
                        val value = dailyAverages[index].average!!
                        val color = when {
                            value in 4f..7f -> colorScheme.primary
                            value > 7f -> colorScheme.error
                            else -> Color(0xFF8f84d4)
                        }
                        drawCircle(
                            color = color,
                            radius = 3.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DietCaloriesChart(
    dailyCalories: List<com.example.fit5046_app.DailyCalories>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    if (dailyCalories.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No diet data available",
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        return
    }
    
    val maxValue = dailyCalories.maxOf { it.calories }.toFloat()
    val range = maxValue * 1.2f // Add 20% padding
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val chartHeight = canvasHeight - 20f
            val chartWidth = canvasWidth - 20f
            val barWidth = chartWidth / dailyCalories.size * 0.6f
            
            // Draw bars
            dailyCalories.forEachIndexed { index, daily ->
                val x = 10f + (chartWidth / dailyCalories.size) * index + (chartWidth / dailyCalories.size - barWidth) / 2
                val normalizedValue = daily.calories.toFloat() / range
                val barHeight = normalizedValue * chartHeight
                val y = 10f + chartHeight - barHeight
                
                drawRect(
                    color = colorScheme.secondary,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
            }
        }
    }
}

@Composable
fun ExerciseMinutesChart(
    dailyMinutes: List<com.example.fit5046_app.DailyExercise>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    if (dailyMinutes.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No exercise data available",
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        return
    }
    
    val maxValue = dailyMinutes.maxOf { it.minutes }.toFloat()
    val range = maxValue * 1.2f // Add 20% padding
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val chartHeight = canvasHeight - 20f
            val chartWidth = canvasWidth - 20f
            val barWidth = chartWidth / dailyMinutes.size * 0.6f
            
            // Draw bars
            dailyMinutes.forEachIndexed { index, daily ->
                val x = 10f + (chartWidth / dailyMinutes.size) * index + (chartWidth / dailyMinutes.size - barWidth) / 2
                val normalizedValue = daily.minutes.toFloat() / range
                val barHeight = normalizedValue * chartHeight
                val y = 10f + chartHeight - barHeight
                
                drawRect(
                    color = colorScheme.tertiary,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
            }
        }
    }
}

@Composable
fun ExerciseTypesChart(
    exerciseTypes: Map<com.example.fit5046_app.model.ExerciseType, Int>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    if (exerciseTypes.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No exercise data available",
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        return
    }
    
    val totalMinutes = exerciseTypes.values.sum()
    val colors = listOf(
        colorScheme.primary,
        colorScheme.secondary,
        colorScheme.tertiary,
        Color(0xFF8f84d4),
        Color(0xffff9713)
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2
            val radius = minOf(canvasWidth, canvasHeight) / 2 - 20f
            
            var startAngle = -90f
            var index = 0
            
            exerciseTypes.forEach { (exerciseType, minutes) ->
                val sweepAngle = (minutes.toFloat() / totalMinutes) * 360f
                val color = colors[index % colors.size]
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )
                
                startAngle += sweepAngle
                index++
            }
        }
        
        // Legend
        Column(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            exerciseTypes.entries.forEachIndexed { index, entry ->
                val exerciseType = entry.key
                val minutes = entry.value
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                colors[index % colors.size],
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${exerciseType.displayName}: ${minutes}min",
                        fontSize = 10.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HealthCorrelationChart(
    glucoseAnalysis: com.example.fit5046_app.GlucoseAnalysis,
    dietAnalysis: com.example.fit5046_app.DietAnalysis,
    exerciseAnalysis: com.example.fit5046_app.ExerciseAnalysis,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    val dailyData = glucoseAnalysis.dailyAverages.map { glucoseData ->
        val date = glucoseData.date
        
        val glucose = glucoseData.average ?: 0f
        val calories = dietAnalysis.dailyCalories.find { it.date == date }?.calories ?: 0
        val exercise = exerciseAnalysis.dailyMinutes.find { it.date == date }?.minutes ?: 0
        
        DailyHealthData(
            date = date,
            glucose = glucose,
            calories = calories,
            exercise = exercise
        )
    }
    
    if (dailyData.all { it.glucose == 0f && it.calories == 0 && it.exercise == 0 }) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No health data available for correlation analysis",
                color = colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val chartHeight = canvasHeight - 60f
            val chartWidth = canvasWidth - 40f
            
            val maxGlucose = dailyData.maxOfOrNull { it.glucose } ?: 10f
            val maxCalories = dailyData.maxOfOrNull { it.calories } ?: 2000
            val maxExercise = dailyData.maxOfOrNull { it.exercise } ?: 60
            
            val gridColor = colorScheme.outline.copy(alpha = 0.1f)
            for (i in 0..4) {
                val y = 20f + (chartHeight / 4) * i
                drawLine(
                    color = gridColor,
                    start = Offset(20f, y),
                    end = Offset(canvasWidth - 20f, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            if (dailyData.any { it.glucose > 0 }) {
                val glucosePoints = dailyData.mapIndexed { index, data ->
                    val x = 20f + (chartWidth / (dailyData.size - 1)) * index
                    val normalizedGlucose = data.glucose / maxGlucose
                    val y = 20f + chartHeight - (normalizedGlucose * chartHeight)
                    Offset(x, y)
                }
                
                for (i in 0 until glucosePoints.size - 1) {
                    if (dailyData[i].glucose > 0 && dailyData[i + 1].glucose > 0) {
                        drawLine(
                            color = colorScheme.primary,
                            start = glucosePoints[i],
                            end = glucosePoints[i + 1],
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
                
                glucosePoints.forEachIndexed { index, point ->
                    if (dailyData[index].glucose > 0) {
                        drawCircle(
                            color = colorScheme.primary,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
            
            val barWidth = chartWidth / dailyData.size * 0.3f
            dailyData.forEachIndexed { index, data ->
                if (data.calories > 0) {
                    val x = 20f + (chartWidth / dailyData.size) * index + (chartWidth / dailyData.size - barWidth) / 2
                    val normalizedCalories = data.calories.toFloat() / maxCalories
                    val barHeight = normalizedCalories * chartHeight * 0.4f // Scale down to not overlap
                    val y = 20f + chartHeight - barHeight
                    
                    drawRect(
                        color = colorScheme.secondary.copy(alpha = 0.6f),
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                    )
                }
            }
            
            dailyData.forEachIndexed { index, data ->
                if (data.exercise > 0) {
                    val x = 20f + (chartWidth / dailyData.size) * index
                    val normalizedExercise = data.exercise.toFloat() / maxExercise
                    val indicatorSize = normalizedExercise * 8.dp.toPx() + 2.dp.toPx()
                    val y = 20f + chartHeight + 10f
                    
                    drawCircle(
                        color = colorScheme.tertiary,
                        radius = indicatorSize,
                        center = Offset(x, y)
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colorScheme.primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Glucose (mmol/L)",
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colorScheme.secondary.copy(alpha = 0.6f), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Calories (bars)",
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colorScheme.tertiary, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Exercise (circles)",
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
        
        val insights = generateCorrelationInsights(glucoseAnalysis, dietAnalysis, exerciseAnalysis)
        if (insights.isNotEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = "📊 Insights",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                insights.take(2).forEach { insight ->
                    Text(
                        text = "• $insight",
                        fontSize = 10.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

private fun generateCorrelationInsights(
    glucose: com.example.fit5046_app.GlucoseAnalysis,
    diet: com.example.fit5046_app.DietAnalysis,
    exercise: com.example.fit5046_app.ExerciseAnalysis
): List<String> {
    val insights = mutableListOf<String>()
    
    if (glucose.averageGlucose > 0) {
        when {
            glucose.averageGlucose < 4f -> insights.add("Low glucose levels detected")
            glucose.averageGlucose > 7f -> insights.add("High glucose levels - consider diet/exercise")
            else -> insights.add("Glucose levels within healthy range")
        }
    }
    
    // Exercise vs Glucose correlation
    if (exercise.averageMinutesPerDay > 0 && glucose.averageGlucose > 0) {
        when {
            exercise.averageMinutesPerDay > 30 && glucose.averageGlucose < 6f -> 
                insights.add("Good exercise routine supporting glucose control")
            exercise.averageMinutesPerDay < 15 && glucose.averageGlucose > 6.5f -> 
                insights.add("Consider increasing exercise for better glucose control")
        }
    }
    
    // Diet insights
    if (diet.averageDailyCalories > 0) {
        when {
            diet.averageDailyCalories > 2500 -> insights.add("High calorie intake - monitor glucose impact")
            diet.averageDailyCalories < 1500 -> insights.add("Low calorie intake - ensure adequate nutrition")
        }
    }
    
    return insights
}

data class DailyHealthData(
    val date: java.time.LocalDate,
    val glucose: Float,
    val calories: Int,
    val exercise: Int
)
