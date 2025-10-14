package com.example.fit5046_app.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fit5046_app.model.FoodCategory
import com.example.fit5046_app.model.FoodDatabase
import com.example.fit5046_app.model.FoodItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchComponent(
    selectedFoods: List<FoodItemWithQuantity>,
    onFoodSelected: (FoodItemWithQuantity) -> Unit,
    onFoodRemoved: (String) -> Unit,
    onQuantityChanged: (String, Float) -> Unit,
    onCustomFoodAdded: (FoodItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<FoodCategory?>(null) }
    var showAddCustomFood by remember { mutableStateOf(false) }
    var showSearchResults by remember { mutableStateOf(false) }

    val searchResults = remember(searchQuery, selectedCategory) {
        val foods = if (selectedCategory != null) {
            FoodDatabase.getFoodsByCategory(selectedCategory!!)
        } else {
            FoodDatabase.searchFoods(searchQuery)
        }
        foods
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                showSearchResults = it.isNotBlank()
            },
            label = { Text("Search Foods") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        showSearchResults = false
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Category Filter
        CategoryFilter(
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = if (selectedCategory == category) null else category
                showSearchResults = true
            }
        )

        // Add Custom Food Button
        OutlinedButton(
            onClick = { showAddCustomFood = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Custom Food")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Custom Food")
        }

        // Search Results
        if (showSearchResults) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(searchResults) { food ->
                        FoodSearchResultItem(
                            food = food,
                            onFoodSelected = { selectedFood ->
                                onFoodSelected(FoodItemWithQuantity(selectedFood, 100f))
                                showSearchResults = false
                                searchQuery = ""
                            }
                        )
                    }
                }
            }
        }

        // Selected Foods
        if (selectedFoods.isNotEmpty()) {
            Text(
                text = "Selected Foods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            selectedFoods.forEach { foodWithQuantity ->
                SelectedFoodItem(
                    foodWithQuantity = foodWithQuantity,
                    onQuantityChanged = { quantity ->
                        onQuantityChanged(foodWithQuantity.food.id, quantity)
                    },
                    onRemove = {
                        onFoodRemoved(foodWithQuantity.food.id)
                    }
                )
            }
        }
    }

    // Add Custom Food Dialog
    if (showAddCustomFood) {
        AddCustomFoodDialog(
            onDismiss = { showAddCustomFood = false },
            onFoodAdded = { customFood ->
                onCustomFoodAdded(customFood)
                showAddCustomFood = false
            }
        )
    }
}

@Composable
private fun CategoryFilter(
    selectedCategory: FoodCategory?,
    onCategorySelected: (FoodCategory) -> Unit
) {
    LazyColumn(
        modifier = Modifier.height(120.dp)
    ) {
        items(FoodCategory.values().filter { it != FoodCategory.CUSTOM }) { category ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) },
                selected = selectedCategory == category,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FoodSearchResultItem(
    food: FoodItem,
    onFoodSelected: (FoodItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFoodSelected(food) }
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${food.caloriesPer100g} cal per 100g",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SelectedFoodItem(
    foodWithQuantity: FoodItemWithQuantity,
    onQuantityChanged: (Float) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = foodWithQuantity.food.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Quantity: ${foodWithQuantity.quantity.toInt()}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Calories: ${calculateCalories(foodWithQuantity).toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = foodWithQuantity.quantity.toString(),
                onValueChange = {
                    val quantity = it.toFloatOrNull() ?: 0f
                    onQuantityChanged(quantity)
                },
                label = { Text("g") },
                modifier = Modifier.width(80.dp)
            )

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddCustomFoodDialog(
    onDismiss: () -> Unit,
    onFoodAdded: (FoodItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(FoodCategory.CUSTOM) }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var sodium by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Food") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories per 100g *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fiber,
                        onValueChange = { fiber = it },
                        label = { Text("Fiber (g)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val caloriesInt = calories.toIntOrNull() ?: 0
                    val proteinFloat = protein.toFloatOrNull() ?: 0f
                    val carbsFloat = carbs.toFloatOrNull() ?: 0f
                    val fatFloat = fat.toFloatOrNull() ?: 0f
                    val fiberFloat = fiber.toFloatOrNull() ?: 0f
                    val sugarFloat = sugar.toFloatOrNull() ?: 0f
                    val sodiumFloat = sodium.toFloatOrNull() ?: 0f

                    if (name.isNotBlank() && caloriesInt > 0) {
                        val customFood = FoodItem(
                            id = "custom_${System.currentTimeMillis()}",
                            name = name,
                            category = FoodCategory.CUSTOM,
                            caloriesPer100g = caloriesInt,
                            proteinPer100g = proteinFloat,
                            carbsPer100g = carbsFloat,
                            fatPer100g = fatFloat,
                            fiberPer100g = fiberFloat,
                            sugarPer100g = sugarFloat,
                            sodiumPer100g = sodiumFloat,
                            isCustom = true
                        )
                        onFoodAdded(customFood)
                    }
                },
                enabled = name.isNotBlank() && calories.isNotBlank() && calories.toIntOrNull() != null
            ) {
                Text("Add Food")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class FoodItemWithQuantity(
    val food: FoodItem,
    val quantity: Float // in grams
)

fun calculateCalories(foodWithQuantity: FoodItemWithQuantity): Float {
    return (foodWithQuantity.food.caloriesPer100g * foodWithQuantity.quantity / 100f)
}

fun calculateProtein(foodWithQuantity: FoodItemWithQuantity): Float {
    return (foodWithQuantity.food.proteinPer100g * foodWithQuantity.quantity / 100f)
}

fun calculateCarbs(foodWithQuantity: FoodItemWithQuantity): Float {
    return (foodWithQuantity.food.carbsPer100g * foodWithQuantity.quantity / 100f)
}

fun calculateFat(foodWithQuantity: FoodItemWithQuantity): Float {
    return (foodWithQuantity.food.fatPer100g * foodWithQuantity.quantity / 100f)
}
