package com.example.fit5046_app.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EnhancedNutritionCard(
    calories: Int,
    protein: Float,
    carbs: Float,
    fat: Float,
    foodCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📊 Nutrition Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "From $foodCount food${if (foodCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Total calories highlight
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$calories",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "calories",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Macro nutrients grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedNutritionInfo(
                    label = "Protein",
                    value = protein.toInt(),
                    unit = "g",
                    icon = "💪",
                    color = MaterialTheme.colorScheme.secondary
                )
                EnhancedNutritionInfo(
                    label = "Carbs",
                    value = carbs.toInt(),
                    unit = "g",
                    icon = "🌾",
                    color = MaterialTheme.colorScheme.tertiary
                )
                EnhancedNutritionInfo(
                    label = "Fat",
                    value = fat.toInt(),
                    unit = "g",
                    icon = "🥑",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Health insights
            if (calories > 0) {
                HealthInsights(
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat
                )
            }
        }
    }
}

@Composable
private fun EnhancedNutritionInfo(
    label: String,
    value: Int,
    unit: String,
    icon: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun HealthInsights(
    calories: Int,
    protein: Float,
    carbs: Float,
    fat: Float
) {
    val insights = mutableListOf<String>()

    // Calorie insights
    when {
        calories < 200 -> insights.add("Light meal")
        calories < 500 -> insights.add("Moderate meal")
        calories < 800 -> insights.add("Hearty meal")
        else -> insights.add("Large meal")
    }

    // Protein insights
    when {
        protein > 30 -> insights.add("High protein")
        protein > 15 -> insights.add("Good protein")
        else -> insights.add("Low protein")
    }

    // Balance insights
    val proteinRatio = (protein * 4) / calories * 100
    val carbRatio = (carbs * 4) / calories * 100
    val fatRatio = (fat * 9) / calories * 100

    when {
        proteinRatio > 25 -> insights.add("Protein-rich")
        carbRatio > 60 -> insights.add("Carb-focused")
        fatRatio > 40 -> insights.add("Fat-rich")
        else -> insights.add("Balanced")
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💡",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Insights: ${insights.joinToString(" • ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
