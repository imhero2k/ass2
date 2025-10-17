package com.example.fit5046_app

import com.example.fit5046_app.network.NewsApiService
import com.example.fit5046_app.network.NewsArticleDto
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsRepository {
    private val apiKey = "e7f83da6f136427a95ddfa0f615d72bb"
    private val newsApiService: NewsApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        newsApiService = retrofit.create(NewsApiService::class.java)
    }

    suspend fun fetchDiabetesNews(): List<BlogArticle> = try {
        println("DEBUG: Fetching diabetes articles...")
        val response = newsApiService.searchNews(
            query = "diabetes",
            apiKey = apiKey
        )
        println("DEBUG: Got ${response.articles.size} articles from API")

        val filtered = response.articles
            .filter { isRelevantToDiabetes(it) }
        println("DEBUG: After filtering: ${filtered.size} articles")

        val mapped = filtered.map { dto ->
            dtoToBlogArticle(dto)
        }.take(50)
        println("DEBUG: Returning ${mapped.size} articles to UI")
        mapped
    } catch (e: Exception) {
        println("DEBUG: Error fetching articles: ${e.message}")
        e.printStackTrace()
        emptyList()
    }


    private fun isRelevantToDiabetes(article: NewsArticleDto): Boolean {
        val diabetesKeywords = listOf(
            "diabetes",
            "blood sugar",
            "glucose",
            "insulin",
            "type 2 diabetes",
            "type 1 diabetes",
            "diabetic",
            "hyperglycemia",
            "hypoglycemia"
        )

        val text = "${article.title} ${article.description}".lowercase()
        return diabetesKeywords.any { text.contains(it) }
    }

    private fun dtoToBlogArticle(dto: NewsArticleDto): BlogArticle {
        val readTime = estimateReadTime(dto.content)
        val category = categorizeArticle(dto)

        return BlogArticle(
            title = dto.title,
            excerpt = dto.description ?: dto.content?.take(150) ?: "Health news about diabetes",
            content = dto.content ?: "",
            author = dto.author ?: dto.source.name,
            publishDate = parseDate(dto.publishedAt),
            category = category,
            readTime = readTime,
            likes = 0,
            views = 0,
            imageEmoji = "🩺",
            articleUrl = dto.url
        )
    }

    private fun categorizeArticle(article: NewsArticleDto): String {
        val text = "${article.title} ${article.description}".lowercase()

        return when {
            text.contains(Regex("\\b(nutrition|diet|food|weight loss|meal|eat)\\b")) -> "Nutrition"
            text.contains(Regex("\\b(exercise|workout|running|yoga|gym|fitness|training)\\b")) -> "Exercise"
            text.contains(Regex("\\b(mental|stress|anxiety|depression|mood|psychological)\\b")) -> "Mental Health"
            text.contains(Regex("\\b(wellness|wellbeing|health|recovery|treatment)\\b")) -> "Wellness"
            else -> "Diabetes"
        }
    }

    private fun parseDate(dateString: String): Date {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    private fun estimateReadTime(content: String?): String {
        if (content.isNullOrEmpty()) return "5 min"
        val wordCount = content.split("\\s+".toRegex()).size
        val minutes = maxOf(1, wordCount / 200)
        return "$minutes min"
    }
}
