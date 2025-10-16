package com.example.fit5046_app.network

data class NewsApiResponse(
    val articles: List<NewsArticleDto>
)

data class NewsArticleDto(
    val source: SourceDto,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
)

data class SourceDto(
    val id: String?,
    val name: String
)