package com.example.fit5046_app.service

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class NutritionAnalysisService {
    
    private val imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    
    
    data class FoodItem(
        val name: String,
        val confidence: Float,
        // Per 100g reference values for better accuracy
        val caloriesPer100g: Int? = null,
        val proteinPer100g: Float? = null,
        val fatPer100g: Float? = null,
        val carbsPer100g: Float? = null,
        // A sensible default portion guess (grams) users can adjust in UI
        val defaultPortionGrams: Int = 100
    )
    
    
    data class NutritionAnalysisResult(
        val foodItems: List<FoodItem>,
        // Totals here are based on defaultPortionGrams per detected item.
        // The UI should prefer to scale from per-100g values.
        val totalCalories: Int,
        val totalProtein: Float,
        val totalFat: Float,
        val totalCarbs: Float,
        val confidence: Float
    )
    
    
    suspend fun analyzeImage(bitmap: Bitmap): Result<NutritionAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            
            // Use ML Kit to label the image
            val labels = suspendCancellableCoroutine<List<com.google.mlkit.vision.label.ImageLabel>> { continuation ->
                imageLabeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        continuation.resume(labels)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
            
            // Filter for food-related labels and convert to FoodItems
            val foodItems = labels
                .filter { label -> 
                    isFoodRelated(label.text) && label.confidence > 0.3f 
                }
                .map { label ->
                    val foodName = label.text
                    val confidence = label.confidence
                    
                    
                    val nutrition = referencePer100g(foodName)
                    
                    FoodItem(
                        name = foodName,
                        confidence = confidence,
                        caloriesPer100g = nutrition.calories,
                        proteinPer100g = nutrition.protein,
                        fatPer100g = nutrition.fat,
                        carbsPer100g = nutrition.carbs,
                        defaultPortionGrams = nutrition.defaultPortionGrams
                    )
                }
                .sortedByDescending { it.confidence }
            
            if (foodItems.isEmpty()) {
                return@withContext Result.failure(
                    Exception("No food items detected in the image. Please try a clearer photo of food.")
                )
            }
            
            // Calculate 
            val totalCalories = foodItems.fold(0) { acc, item ->
                val per100 = item.caloriesPer100g ?: 0
                val grams = item.defaultPortionGrams
                acc + (per100 * grams / 100)
            }
            val totalProtein = foodItems.fold(0f) { acc, item ->
                val per100 = item.proteinPer100g ?: 0f
                val grams = item.defaultPortionGrams
                acc + (per100 * grams / 100f)
            }
            val totalFat = foodItems.fold(0f) { acc, item ->
                val per100 = item.fatPer100g ?: 0f
                val grams = item.defaultPortionGrams
                acc + (per100 * grams / 100f)
            }
            val totalCarbs = foodItems.fold(0f) { acc, item ->
                val per100 = item.carbsPer100g ?: 0f
                val grams = item.defaultPortionGrams
                acc + (per100 * grams / 100f)
            }
            val avgConfidence = foodItems.map { it.confidence }.average().toFloat()
            
            val result = NutritionAnalysisResult(
                foodItems = foodItems,
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalFat = totalFat,
                totalCarbs = totalCarbs,
                confidence = avgConfidence
            )
            
            Log.d("NutritionAnalysis", "Analysis completed: ${foodItems.size} items detected")
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e("NutritionAnalysis", "Error analyzing image", e)
            Result.failure(e)
        }
    }
    
    
    private fun isFoodRelated(label: String): Boolean {
        val foodKeywords = listOf(
            "food", "meal", "dish", "plate", "bowl", "cup", "glass",
            "apple", "banana", "orange", "fruit", "vegetable", "salad",
            "bread", "pizza", "pasta", "rice", "noodle", "sandwich",
            "meat", "chicken", "beef", "fish", "egg", "cheese",
            "milk", "yogurt", "cereal", "cake", "cookie", "dessert",
            "soup", "stew", "curry", "burger", "fries", "potato",
            "tomato", "lettuce", "onion", "carrot", "broccoli",
            "coffee", "tea", "juice", "soda", "water", "drink"
        )
        
        val lowerLabel = label.lowercase()
        return foodKeywords.any { keyword -> lowerLabel.contains(keyword) }
    }
    
    
    private fun referencePer100g(foodName: String): NutritionData {
        val lowerName = foodName.lowercase()
        
        return when {
            // Values for reference
            lowerName.contains("apple") -> NutritionData(52, 0.3f, 0.2f, 14.0f, defaultPortionGrams = 150)
            lowerName.contains("banana") -> NutritionData(89, 1.1f, 0.3f, 23.0f, defaultPortionGrams = 120)
            lowerName.contains("orange") -> NutritionData(47, 0.9f, 0.1f, 12.0f, defaultPortionGrams = 130)
            lowerName.contains("bread") -> NutritionData(265, 9.0f, 3.2f, 49.0f, defaultPortionGrams = 40)
            lowerName.contains("rice") -> NutritionData(130, 2.7f, 0.3f, 28.0f, defaultPortionGrams = 150)
            lowerName.contains("pasta") -> NutritionData(131, 5.0f, 1.1f, 25.0f, defaultPortionGrams = 180)
            lowerName.contains("chicken") -> NutritionData(165, 31.0f, 3.6f, 0.0f, defaultPortionGrams = 120)
            lowerName.contains("beef") -> NutritionData(250, 26.0f, 15.0f, 0.0f, defaultPortionGrams = 120)
            lowerName.contains("fish") -> NutritionData(206, 22.0f, 12.0f, 0.0f, defaultPortionGrams = 120)
            lowerName.contains("egg") -> NutritionData(155, 13.0f, 11.0f, 1.1f, defaultPortionGrams = 50)
            lowerName.contains("cheese") -> NutritionData(402, 25.0f, 33.0f, 1.3f, defaultPortionGrams = 30)
            lowerName.contains("milk") -> NutritionData(42, 3.4f, 1.0f, 5.0f, defaultPortionGrams = 244)
            lowerName.contains("yogurt") -> NutritionData(59, 10.0f, 0.4f, 3.6f, defaultPortionGrams = 170)
            lowerName.contains("salad") -> NutritionData(20, 1.0f, 0.2f, 4.0f, defaultPortionGrams = 150)
            lowerName.contains("pizza") -> NutritionData(266, 11.0f, 10.0f, 33.0f, defaultPortionGrams = 120)
            lowerName.contains("burger") -> NutritionData(295, 17.0f, 14.0f, 24.0f, defaultPortionGrams = 200)
            lowerName.contains("fries") -> NutritionData(312, 3.4f, 15.0f, 41.0f, defaultPortionGrams = 117)
            lowerName.contains("cake") -> NutritionData(350, 5.0f, 16.0f, 50.0f, defaultPortionGrams = 80)
            lowerName.contains("cookie") -> NutritionData(488, 6.0f, 24.0f, 64.0f, defaultPortionGrams = 30)
            lowerName.contains("coffee") -> NutritionData(2, 0.3f, 0.0f, 0.0f, defaultPortionGrams = 240)
            lowerName.contains("tea") -> NutritionData(1, 0.0f, 0.0f, 0.0f, defaultPortionGrams = 240)
            lowerName.contains("juice") -> NutritionData(45, 0.5f, 0.1f, 10.0f, defaultPortionGrams = 240)
            lowerName.contains("soda") -> NutritionData(41, 0.0f, 0.0f, 11.0f, defaultPortionGrams = 355)
            lowerName.contains("water") -> NutritionData(0, 0.0f, 0.0f, 0.0f, defaultPortionGrams = 240)
            else -> NutritionData(100, 5.0f, 3.0f, 15.0f, defaultPortionGrams = 100) 
        }
    }
    
    
    private data class NutritionData(
        val calories: Int, 
        val protein: Float, 
        val fat: Float, 
        val carbs: Float, 
        val defaultPortionGrams: Int
    )
    
    
    fun cleanup() {
        imageLabeler.close()
    }
}