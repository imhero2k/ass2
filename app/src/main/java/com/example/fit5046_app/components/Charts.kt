package com.example.fit5046_app.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    glucoseTrend: List<Float>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    if (glucoseTrend.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No glucose data available",
                color = colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }
    
    val maxValue = glucoseTrend.maxOrNull() ?: 10f
    val minValue = glucoseTrend.minOrNull() ?: 0f
    val range = maxValue - minValue
    val padding = range * 0.1f
    
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
            val chartHeight = canvasHeight - 40f
            val chartWidth = canvasWidth - 40f
            
            // Draw grid lines
            val gridColor = colorScheme.outline.copy(alpha = 0.2f)
            for (i in 0..4) {
                val y = 20f + (chartHeight / 4) * i
                drawLine(
                    color = gridColor,
                    start = Offset(20f, y),
                    end = Offset(canvasWidth - 20f, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Draw glucose line
            if (glucoseTrend.size > 1) {
                val points = glucoseTrend.mapIndexed { index, value ->
                    val x = 20f + (chartWidth / (glucoseTrend.size - 1)) * index
                    val normalizedValue = (value - minValue + padding) / (range + 2 * padding)
                    val y = 20f + chartHeight - (normalizedValue * chartHeight)
                    Offset(x, y)
                }
                
                // Draw line
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = colorScheme.primary,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3.dp.toPx()
                    )
                }
                
                // Draw points
                points.forEach { point ->
                    drawCircle(
                        color = colorScheme.primary,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }
        
        // Y-axis labels
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            repeat(5) { i ->
                val value = maxValue - (range / 4) * i
                Text(
                    text = String.format("%.1f", value),
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun WeeklyGlucoseChart(
    weeklyTrend: List<com.example.fit5046_app.DailyGlucose>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    if (weeklyTrend.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No weekly data available",
                color = colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }
    
    val validData = weeklyTrend.filter { it.averageGlucose != null }
    if (validData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No valid glucose data",
                color = colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }
    
    val maxValue = validData.maxOf { it.averageGlucose!! }
    val minValue = validData.minOf { it.averageGlucose!! }
    val range = maxValue - minValue
    val padding = range * 0.1f
    
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
            val barWidth = chartWidth / weeklyTrend.size * 0.6f
            
            // Draw grid lines
            val gridColor = colorScheme.outline.copy(alpha = 0.2f)
            for (i in 0..4) {
                val y = 20f + (chartHeight / 4) * i
                drawLine(
                    color = gridColor,
                    start = Offset(20f, y),
                    end = Offset(canvasWidth - 20f, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Draw bars
            weeklyTrend.forEachIndexed { index, daily ->
                val x = 20f + (chartWidth / weeklyTrend.size) * index + (chartWidth / weeklyTrend.size - barWidth) / 2
                val value = daily.averageGlucose ?: 0f
                val normalizedValue = (value - minValue + padding) / (range + 2 * padding)
                val barHeight = normalizedValue * chartHeight
                val y = 20f + chartHeight - barHeight
                
                val color = if (value in 4f..7f) {
                    colorScheme.primary
                } else {
                    colorScheme.error
                }
                
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
            }
        }
        
        // Y-axis labels
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            repeat(5) { i ->
                val value = maxValue - (range / 4) * i
                Text(
                    text = String.format("%.1f", value),
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        
        // X-axis labels
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weeklyTrend.forEach { daily ->
                Text(
                    text = daily.date.format(DateTimeFormatter.ofPattern("MMM dd")),
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MealCaloriesChart(
    mealCalories: com.example.fit5046_app.MealCalories,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    val calories = listOf(
        mealCalories.breakfast.toFloat(),
        mealCalories.lunch.toFloat(),
        mealCalories.dinner.toFloat(),
        mealCalories.snack.toFloat()
    )
    
    val maxValue = calories.maxOrNull() ?: 100f
    val range = maxValue * 1.2f // Add 20% padding
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val chartHeight = canvasHeight - 40f
            val chartWidth = canvasWidth - 40f
            val barWidth = chartWidth / meals.size * 0.6f
            
            // Draw grid lines
            val gridColor = colorScheme.outline.copy(alpha = 0.2f)
            for (i in 0..4) {
                val y = 20f + (chartHeight / 4) * i
                drawLine(
                    color = gridColor,
                    start = Offset(20f, y),
                    end = Offset(canvasWidth - 20f, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Draw bars
            calories.forEachIndexed { index, value ->
                val x = 20f + (chartWidth / meals.size) * index + (chartWidth / meals.size - barWidth) / 2
                val normalizedValue = value / range
                val barHeight = normalizedValue * chartHeight
                val y = 20f + chartHeight - barHeight
                
                drawRect(
                    color = colorScheme.secondary,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
            }
        }
        
        // Y-axis labels
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            repeat(5) { i ->
                val value = (range / 4) * (4 - i)
                Text(
                    text = "${value.toInt()}",
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        
        // X-axis labels
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            meals.forEach { meal ->
                Text(
                    text = meal,
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun WaterIntakeProgress(
    waterIntake: Int,
    target: Int = 2000,
    modifier: Modifier = Modifier
) {
    val progress = (waterIntake.toFloat() / target).coerceAtMost(1f)
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Water Intake",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 8.dp,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${waterIntake}ml",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "of ${target}ml",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
