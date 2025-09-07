package com.example.fit5046_app

import androidx.annotation.DrawableRes


enum class Destination(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int
) {
    DASHBOARD("dashboard", "Dashboard", R.drawable.home_24px),
    DIARY("diary", "Diary", R.drawable.diary_24px),
    LOG("log", "Log", R.drawable.add_24px),
    REPORTS("reports", "Reports", R.drawable.reports_24px),
    HEALTHBLOG("healthblog", "Health Blog", R.drawable.news_24px),
    USERSETTING("usersetting", "User", R.drawable.user_24px)
}