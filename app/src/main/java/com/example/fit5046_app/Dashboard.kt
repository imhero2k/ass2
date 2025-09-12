package com.example.fit5046_app


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.fit5046_app.R
import com.example.fit5046_app.ui.theme.FIT5046_appTheme

@Composable
fun Dashboard() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        /* ── reminder section ─────────────────────────────────────────── */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔔",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reminder",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Take your insulin shot at 6:00 PM",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Details",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        /* ── header with date navigation ─────────────────────────────── */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.primary)
            }

            Text(
                text = "Today",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.DateRange, contentDescription = "Calendar",
                        tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        /* ── main content card ───────────────────────────────────────── */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                /* ── glucose target with metrics grid ──── */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Glucose Target",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = {}, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More",
                            tint = Color.Gray)
                    }
                }

                Spacer(Modifier.height(8.dp))

                /* ── metrics grid around central circle ─── */
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        // Top row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricCard("Insulin", "12u", "💉")
                            Text(
                                text = "7.2",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard("Breakfast", "45g", "🍳")
                        }

                        Spacer(Modifier.height(16.dp))

                        // Middle row with central circle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MetricCard("Readings", "8", "📊")

                            // Central glucose reading
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = RoundedCornerShape(60.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "6.8",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "mmol/L",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            MetricCard("Lunch", "38g", "🥗")
                        }

                        Spacer(Modifier.height(16.dp))

                        // Bottom row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricCard("Water", "6", "💧")
                            Text(
                                text = "Target: 7.0",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard("Dinner", "42g", "🍽️")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                /* ── analysis section ────────────────────── */
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📈", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "My Analysis: ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "on target",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                /* ── chart with image ───────────────────── */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Replace with your dummy_chart image
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 40.sp
                        )
                        Text(
                            text = "Blood Glucose Levels",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Past 7 Days Trend",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* ── summary cards ────────────────────────── */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        icon = "🩺",
                        title = "Blood Sugar",
                        subtitle = "Last reading: Today",
                        value = "6.8 mmol/L",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        icon = "🎯",
                        title = "My Goal",
                        subtitle = "Maintain levels 4-7 mmol/L",
                        value = "",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

/* ─── helper composables ──────────────────────────────────────────────────── */
@Composable
private fun MetricCard(title: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = icon, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SummaryCard(
    icon: String,
    title: String,
    subtitle: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                if (value.isNotEmpty()) {
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/* ─── preview ─────────────────────────────────────────────────────────────── */
@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    FIT5046_appTheme { Dashboard() }
}
