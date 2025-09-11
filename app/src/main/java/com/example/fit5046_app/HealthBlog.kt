package com.example.fit5046_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// Data class for blog articles
data class BlogArticle(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val excerpt: String,
    val content: String,
    val author: String,
    val publishDate: Date,
    val category: String,
    val readTime: String,
    val likes: Int = 0,
    val views: Int = 0,
    val imageEmoji: String
)

@Composable
fun HealthBlog() {
    var articles by remember { mutableStateOf(getSampleArticles()) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Nutrition", "Exercise", "Mental Health", "Diabetes", "Wellness")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Health Blog",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Category Filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Articles List
        val filteredArticles = if (selectedCategory == "All") {
            articles
        } else {
            articles.filter { it.category == selectedCategory }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredArticles) { article ->
                BlogArticleCard(
                    article = article,
                    onLike = {
                        articles = articles.map {
                            if (it.id == article.id) it.copy(likes = it.likes + 1)
                            else it
                        }
                    },
                    onShare = { /* TODO: Implement share functionality */ },
                    onRead = { /* TODO: Navigate to full article */ }
                )
            }
        }
    }
}

@Composable
fun BlogArticleCard(
    article: BlogArticle,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRead() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Article Image/Emoji
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = article.imageEmoji,
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Badge
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = article.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Excerpt
            Text(
                text = article.excerpt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Author and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "By ${article.author}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(article.publishDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${article.readTime} read",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Like Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = onLike) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Like",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = article.likes.toString(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Views
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Views",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = article.views.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Share Button
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Sample data
fun getSampleArticles(): List<BlogArticle> {
    return listOf(
        BlogArticle(
            title = "Understanding Blood Sugar Levels: A Complete Guide",
            excerpt = "Learn about normal blood sugar ranges, how to monitor your levels, and what factors can affect your glucose readings throughout the day.",
            content = "Full article content about blood sugar levels...",
            author = "Dr. Sarah Johnson",
            publishDate = Date(System.currentTimeMillis() - 86400000), // 1 day ago
            category = "Diabetes",
            readTime = "5 min",
            likes = 42,
            views = 1250,
            imageEmoji = "🩸"
        ),
        BlogArticle(
            title = "10 Superfoods for Better Health",
            excerpt = "Discover the most nutrient-dense foods that can boost your immune system, improve heart health, and provide essential vitamins and minerals.",
            content = "Full article content about superfoods...",
            author = "Nutritionist Mike Chen",
            publishDate = Date(System.currentTimeMillis() - 172800000), // 2 days ago
            category = "Nutrition",
            readTime = "7 min",
            likes = 38,
            views = 980,
            imageEmoji = "🥗"
        ),
        BlogArticle(
            title = "The Mental Health Benefits of Regular Exercise",
            excerpt = "Explore how physical activity can reduce stress, anxiety, and depression while boosting your mood and cognitive function.",
            content = "Full article content about exercise and mental health...",
            author = "Dr. Emily Rodriguez",
            publishDate = Date(System.currentTimeMillis() - 259200000), // 3 days ago
            category = "Mental Health",
            readTime = "6 min",
            likes = 55,
            views = 1450,
            imageEmoji = "🧠"
        ),
        BlogArticle(
            title = "Morning Workout Routines for Busy People",
            excerpt = "Quick and effective exercise routines that fit into your morning schedule, helping you start the day with energy and focus.",
            content = "Full article content about morning workouts...",
            author = "Fitness Coach Alex Thompson",
            publishDate = Date(System.currentTimeMillis() - 345600000), // 4 days ago
            category = "Exercise",
            readTime = "4 min",
            likes = 29,
            views = 750,
            imageEmoji = "🏃"
        ),
        BlogArticle(
            title = "Stress Management Techniques for Better Health",
            excerpt = "Learn practical strategies to reduce stress, improve sleep quality, and maintain emotional well-being in today's fast-paced world.",
            content = "Full article content about stress management...",
            author = "Wellness Expert Lisa Park",
            publishDate = Date(System.currentTimeMillis() - 432000000), // 5 days ago
            category = "Wellness",
            readTime = "8 min",
            likes = 67,
            views = 1890,
            imageEmoji = "🧘"
        ),
        BlogArticle(
            title = "Diabetes-Friendly Meal Planning",
            excerpt = "Simple meal planning strategies to help manage blood sugar levels while enjoying delicious and nutritious foods.",
            content = "Full article content about diabetes meal planning...",
            author = "Registered Dietitian Maria Garcia",
            publishDate = Date(System.currentTimeMillis() - 518400000), // 6 days ago
            category = "Diabetes",
            readTime = "9 min",
            likes = 73,
            views = 2100,
            imageEmoji = "🍽️"
        )
    )
}