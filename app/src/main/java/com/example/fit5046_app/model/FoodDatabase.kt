package com.example.fit5046_app.model

data class FoodItem(
    val id: String,
    val name: String,
    val category: FoodCategory,
    val caloriesPer100g: Int,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val fiberPer100g: Float = 0f,
    val sugarPer100g: Float = 0f,
    val sodiumPer100g: Float = 0f,
    val isCustom: Boolean = false,
    val servingSize: String = "100g"
)

enum class FoodCategory(val displayName: String) {
    FRUITS("Fruits"),
    VEGETABLES("Vegetables"),
    GRAINS("Grains & Cereals"),
    PROTEINS("Proteins"),
    DAIRY("Dairy"),
    SNACKS("Snacks"),
    BEVERAGES("Beverages"),
    DESSERTS("Desserts"),
    FAST_FOOD("Fast Food"),
    CUSTOM("Custom Foods")
}

object FoodDatabase {
    val commonFoods = listOf(
        // Fruits
        FoodItem("1", "Apple", FoodCategory.FRUITS, 52, 0.3f, 13.8f, 0.2f, 2.4f, 10.4f, 1f),
        FoodItem("2", "Banana", FoodCategory.FRUITS, 89, 1.1f, 22.8f, 0.3f, 2.6f, 12.2f, 1f),
        FoodItem("3", "Orange", FoodCategory.FRUITS, 47, 0.9f, 11.8f, 0.1f, 2.4f, 9.4f, 0f),
        FoodItem("4", "Grapes", FoodCategory.FRUITS, 62, 0.6f, 15.5f, 0.2f, 0.9f, 16.0f, 2f),
        FoodItem("5", "Strawberries", FoodCategory.FRUITS, 32, 0.7f, 7.7f, 0.3f, 2.0f, 4.9f, 1f),

        // Vegetables
        FoodItem("6", "Broccoli", FoodCategory.VEGETABLES, 34, 2.8f, 6.6f, 0.4f, 2.6f, 1.5f, 33f),
        FoodItem("7", "Carrot", FoodCategory.VEGETABLES, 41, 0.9f, 9.6f, 0.2f, 2.8f, 4.7f, 69f),
        FoodItem("8", "Spinach", FoodCategory.VEGETABLES, 23, 2.9f, 3.6f, 0.4f, 2.2f, 0.4f, 79f),
        FoodItem("9", "Tomato", FoodCategory.VEGETABLES, 18, 0.9f, 3.9f, 0.2f, 1.2f, 2.6f, 5f),
        FoodItem("10", "Potato", FoodCategory.VEGETABLES, 77, 2.0f, 17.5f, 0.1f, 2.2f, 0.8f, 6f),

        // Grains & Cereals
        FoodItem("11", "White Rice", FoodCategory.GRAINS, 130, 2.7f, 28.0f, 0.3f, 0.4f, 0.1f, 1f),
        FoodItem("12", "Brown Rice", FoodCategory.GRAINS, 111, 2.6f, 23.0f, 0.9f, 1.8f, 0.4f, 5f),
        FoodItem("13", "Oats", FoodCategory.GRAINS, 389, 16.9f, 66.3f, 6.9f, 10.6f, 0.0f, 2f),
        FoodItem("14", "Bread (White)", FoodCategory.GRAINS, 265, 9.0f, 49.0f, 3.2f, 2.7f, 5.0f, 681f),
        FoodItem("15", "Bread (Whole Wheat)", FoodCategory.GRAINS, 247, 13.4f, 41.3f, 4.2f, 6.0f, 4.3f, 681f),

        // Proteins
        FoodItem("16", "Chicken Breast", FoodCategory.PROTEINS, 165, 31.0f, 0.0f, 3.6f, 0.0f, 0.0f, 74f),
        FoodItem("17", "Salmon", FoodCategory.PROTEINS, 208, 25.4f, 0.0f, 12.4f, 0.0f, 0.0f, 44f),
        FoodItem("18", "Eggs", FoodCategory.PROTEINS, 155, 13.0f, 1.1f, 11.0f, 0.0f, 1.1f, 124f),
        FoodItem("19", "Beef (Lean)", FoodCategory.PROTEINS, 250, 26.0f, 0.0f, 15.0f, 0.0f, 0.0f, 72f),
        FoodItem("20", "Tofu", FoodCategory.PROTEINS, 76, 8.1f, 1.9f, 4.8f, 0.3f, 0.6f, 7f),

        // Dairy
        FoodItem("21", "Milk (Whole)", FoodCategory.DAIRY, 61, 3.2f, 4.7f, 3.3f, 0.0f, 4.7f, 40f),
        FoodItem("22", "Milk (Skim)", FoodCategory.DAIRY, 34, 3.4f, 5.0f, 0.2f, 0.0f, 5.0f, 42f),
        FoodItem("23", "Greek Yogurt", FoodCategory.DAIRY, 59, 10.0f, 3.6f, 0.4f, 0.0f, 3.6f, 36f),
        FoodItem("24", "Cheese (Cheddar)", FoodCategory.DAIRY, 403, 25.0f, 1.3f, 33.0f, 0.0f, 0.5f, 621f),

        // Snacks
        FoodItem("25", "Almonds", FoodCategory.SNACKS, 579, 21.2f, 21.6f, 49.9f, 12.5f, 4.4f, 1f),
        FoodItem("26", "Walnuts", FoodCategory.SNACKS, 654, 15.2f, 13.7f, 65.2f, 6.7f, 2.6f, 2f),
        FoodItem("27", "Peanuts", FoodCategory.SNACKS, 567, 25.8f, 16.1f, 49.2f, 8.5f, 4.6f, 18f),

        // Beverages
        FoodItem("28", "Water", FoodCategory.BEVERAGES, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0f),
        FoodItem("29", "Orange Juice", FoodCategory.BEVERAGES, 45, 0.7f, 10.4f, 0.2f, 0.2f, 8.3f, 1f),
        FoodItem("30", "Coffee (Black)", FoodCategory.BEVERAGES, 2, 0.3f, 0.0f, 0.0f, 0.0f, 0.0f, 5f),

        // Fast Food
        FoodItem("31", "McDonald's Big Mac", FoodCategory.FAST_FOOD, 550, 25.0f, 45.0f, 33.0f, 3.0f, 9.0f, 970f),
        FoodItem("32", "Pizza Slice (Cheese)", FoodCategory.FAST_FOOD, 285, 12.0f, 36.0f, 10.0f, 2.0f, 3.0f, 640f),
        FoodItem("33", "French Fries (Medium)", FoodCategory.FAST_FOOD, 365, 4.0f, 63.0f, 17.0f, 5.0f, 0.0f, 246f),

        // Desserts
        FoodItem("34", "Chocolate Cake", FoodCategory.DESSERTS, 371, 4.9f, 51.3f, 15.1f, 2.1f, 51.3f, 350f),
        FoodItem("35", "Ice Cream (Vanilla)", FoodCategory.DESSERTS, 207, 3.5f, 23.6f, 11.0f, 0.7f, 21.2f, 80f),
        FoodItem("36", "Apple Pie", FoodCategory.DESSERTS, 237, 2.4f, 34.0f, 11.0f, 1.3f, 16.0f, 266f)
    )

    fun searchFoods(query: String): List<FoodItem> {
        return if (query.isBlank()) {
            commonFoods
        } else {
            commonFoods.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.category.displayName.contains(query, ignoreCase = true)
            }
        }
    }

    fun getFoodsByCategory(category: FoodCategory): List<FoodItem> {
        return commonFoods.filter { it.category == category }
    }

    fun getFoodById(id: String): FoodItem? {
        return commonFoods.find { it.id == id }
    }
}
