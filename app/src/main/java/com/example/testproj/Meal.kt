package com.example.testproj

data class Meal(
    val id: String = "",         // ID-ul mesei
    val name: String = "",       // Numele mesei
    val calories: Int = 0,       // Caloriile mesei
    val protein: Int = 0,        // Proteinele mesei (în grame)
    val carbs: Int = 0,          // Carbohidrații mesei (în grame)
    val fats: Int = 0,           // Grăsimile mesei (în grame)
    val date: String = ""        // Data mesei
)
