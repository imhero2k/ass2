package com.example.fit5046_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        Dashboard()
    }
}


@Composable
fun Dashboard() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            GlucoseChartCard()
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
            Text(
                text = "Blood Glucose",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Average Glucose
            Text(
                text = "Past 14 Days - All Periods",
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
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Stats Table and Chart
            Row(
                modifier = Modifier.fillMaxWidth(),
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

    val countColor = when {
        label == "High" -> Color(0xffff9713) // orange for high
        label == "Low" -> Color(0xFF8f84d4) // purple for low
        label == "Good" -> MaterialTheme.colorScheme.primary
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