package com.example.testproj

data class Exercise(
    val id: String = "",             // Valoare implicită pentru id
    val name: String = "",           // Valoare implicită pentru name
    val sets: Int = 0,               // Valoare implicită pentru sets
    val reps: Int = 0,               // Valoare implicită pentru reps
    val weight: Double = 0.0,         // Valoare implicită pentru weight
    val date: String = ""// Data la care a fost făcut exercițiul
)
