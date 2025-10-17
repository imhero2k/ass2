package com.example.fit5046_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fit5046_app.data.FoodRecommendationEntity
import com.example.fit5046_app.data.GlucoseSpikeEntity
import com.example.fit5046_app.data.FoodGlucoseCorrelationEntity
import com.example.fit5046_app.service.GlucoseSpikeAnalysisService
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlucoseSpikeAnalysisScreen(
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: GlucoseSpikeAnalysisViewModel = viewModel()
    val analysisData by viewModel.analysisData.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisError by viewModel.analysisError.collectAsState()
    val analysisProgress by viewModel.analysisProgress.collectAsState()
    
    var showDateRangePicker by remember { mutableStateOf(false) }
    var selectedStartDate by remember { mutableStateOf(java.time.LocalDate.now().minusDays(7)) }
    var selectedEndDate by remember { mutableStateOf(java.time.LocalDate.now()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Glucose Spike Analysis",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { showDateRangePicker = true }
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date Range"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "How Glucose Spike Analysis Works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• Detects glucose readings > 7.8 mmol/L as spikes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "• Analyzes food consumption 4 hours before each spike",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "• Calculates correlation using: ((peak glucose - baseline) / baseline) × 100% (capped at 100%)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "• Provides personalized food recommendations based on spike patterns",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Analysis Controls
        AnalysisControls(
            isAnalyzing = isAnalyzing,
            analysisProgress = analysisProgress,
            onRunAnalysis = { viewModel.runAnalysis() },
            onRunDateRangeAnalysis = { start, end -> viewModel.runAnalysisForDateRange(start, end) }
        )
        
        // Error Display
        analysisError?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Analysis Results
        if (analysisData.analysis != null) {
            AnalysisResults(analysisData = analysisData)
        } else {
            EmptyState()
        }
    }
    
    // Date Range Picker Dialog
    if (showDateRangePicker) {
        DateRangePickerDialog(
            onDismiss = { showDateRangePicker = false },
            onConfirm = { startDate, endDate ->
                viewModel.runAnalysisForDateRange(startDate, endDate)
                showDateRangePicker = false
            }
        )
    }
}

@Composable
fun AnalysisControls(
    isAnalyzing: Boolean,
    analysisProgress: Float,
    onRunAnalysis: () -> Unit,
    onRunDateRangeAnalysis: (start: java.time.LocalDateTime, end: java.time.LocalDateTime) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Analysis Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isAnalyzing) {
                Column {
                    Text(
                        text = "Analyzing glucose data...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = analysisProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${(analysisProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            } else {
                Button(
                    onClick = onRunAnalysis,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Analysis")
                }
            }
        }
    }
}

@Composable
fun AnalysisResults(analysisData: AnalysisData) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analysis Summary
        item {
            AnalysisSummaryCard(analysis = analysisData.analysis!!)
        }
        
        // Food Recommendations
        item {
            FoodRecommendationsCard(recommendations = analysisData.recommendations)
        }
        
        // Recent Spikes
        item {
            RecentSpikesCard(spikes = analysisData.spikes.take(5))
        }
        
        // Food Correlations
        item {
            FoodCorrelationsCard(correlations = analysisData.correlations.take(10))
        }
    }
}

@Composable
fun AnalysisSummaryCard(analysis: com.example.fit5046_app.data.GlucoseSpikeAnalysisEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Analysis Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Period Analyzed",
                    value = "${analysis.daysAnalyzed} days",
                    icon = Icons.Default.DateRange
                )
                SummaryItem(
                    label = "Total Readings",
                    value = analysis.totalGlucoseReadings.toString(),
                    icon = Icons.Default.MonitorHeart
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Spikes Detected",
                    value = analysis.totalSpikesDetected.toString(),
                    icon = Icons.Default.TrendingUp
                )
                SummaryItem(
                    label = "Avg Glucose",
                    value = "${analysis.averageGlucose.toInt()} mg/dL",
                    icon = Icons.Default.Speed
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Time in Range",
                    value = "${analysis.timeInRange.toInt()}%",
                    icon = Icons.Default.CheckCircle
                )
                SummaryItem(
                    label = "Analysis Date",
                    value = analysis.analysisDate.format(DateTimeFormatter.ofPattern("MMM dd")),
                    icon = Icons.Default.Schedule
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun FoodRecommendationsCard(recommendations: List<FoodRecommendationEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Food Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (recommendations.isEmpty()) {
                Text(
                    text = "No recommendations available. Run analysis to get personalized food recommendations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Remove duplicates by food item, keeping the one with highest confidence
                val uniqueRecommendations = recommendations
                    .groupBy { it.foodItem }
                    .mapValues { (_, recommendationsForFood) ->
                        recommendationsForFood.maxByOrNull { it.confidence } ?: recommendationsForFood.first()
                    }
                    .values
                    .sortedByDescending { it.confidence }
                
                uniqueRecommendations.forEach { recommendation ->
                    RecommendationItem(recommendation = recommendation)
                    if (recommendation != uniqueRecommendations.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationItem(recommendation: FoodRecommendationEntity) {
    val (icon, color) = when (recommendation.recommendationType) {
        "AVOID" -> Icons.Default.Warning to MaterialTheme.colorScheme.error
        "MODERATE" -> Icons.Default.Info to MaterialTheme.colorScheme.primary
        "SAFE" -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.tertiary
        "BENEFICIAL" -> Icons.Default.ThumbUp to MaterialTheme.colorScheme.secondary
        else -> Icons.Default.Help to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = recommendation.foodItem,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = recommendation.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecentSpikesCard(spikes: List<GlucoseSpikeEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Glucose Spikes (${spikes.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (spikes.isEmpty()) {
                Text(
                    text = "No spikes detected in recent analysis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                spikes.forEach { spike ->
                    SpikeItem(spike = spike)
                    if (spike != spikes.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SpikeItem(spike: GlucoseSpikeEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = spike.spikeTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Spike: +${spike.spikeMagnitude.toInt()} mg/dL",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${spike.spikeValue.toInt()} mg/dL",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Baseline: ${spike.baselineValue.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FoodCorrelationsCard(correlations: List<FoodGlucoseCorrelationEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Food-Glucose Correlations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (correlations.isEmpty()) {
                Text(
                    text = "No correlations found. More data needed for analysis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Remove duplicates by food item, keeping the one with highest correlation score
                val uniqueCorrelations = correlations
                    .groupBy { it.foodItem }
                    .mapValues { (_, correlationsForFood) ->
                        correlationsForFood.maxByOrNull { kotlin.math.abs(it.correlationScore) } ?: correlationsForFood.first()
                    }
                    .values
                    .sortedByDescending { kotlin.math.abs(it.correlationScore) }
                
                uniqueCorrelations.forEach { correlation ->
                    CorrelationItem(correlation = correlation)
                    if (correlation != uniqueCorrelations.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CorrelationItem(correlation: FoodGlucoseCorrelationEntity) {
    val correlationColor = when {
        correlation.correlationScore > 0.5f -> MaterialTheme.colorScheme.error
        correlation.correlationScore > 0.2f -> MaterialTheme.colorScheme.primary
        correlation.correlationScore < -0.2f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(correlationColor.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = correlation.foodItem,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sample size: ${correlation.sampleSize}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${(correlation.correlationScore * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = correlationColor
            )
            Text(
                text = "Spike rate: ${(correlation.spikeFrequency * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Analysis Available",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Run glucose spike analysis to identify which foods may be causing glucose spikes and get personalized recommendations.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (start: java.time.LocalDateTime, end: java.time.LocalDateTime) -> Unit
) {
    var startDate by remember { mutableStateOf(java.time.LocalDate.now().minusDays(7)) }
    var endDate by remember { mutableStateOf(java.time.LocalDate.now()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date Range") },
        text = {
            Column {
                Text("Start Date: ${startDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))}")
                Text("End Date: ${endDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))}")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quick selection buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            endDate = java.time.LocalDate.now()
                            startDate = endDate.minusDays(7)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Last 7 days")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            endDate = java.time.LocalDate.now()
                            startDate = endDate.minusDays(30)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Last 30 days")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
                }
            ) {
                Text("Analyze")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
