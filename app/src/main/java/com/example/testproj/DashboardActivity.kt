package com.example.testproj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)


        val buttonExercise: Button = findViewById(R.id.buttonExercise)
        val buttonMeals: Button = findViewById(R.id.buttonMeals)

        // Setăm listener pentru butonul de Jurnal exerciții
        buttonExercise.setOnClickListener {
            // Lansează activitatea de Jurnal Exerciții
            val intent = Intent(this, ExerciseJournalActivity::class.java)
            startActivity(intent)
        }

        // Setăm listener pentru butonul de Monitorizare Mese
        buttonMeals.setOnClickListener {
            // Lansează activitatea de Monitorizare Mese
            val intent = Intent(this, MealTrackerActivity::class.java)
            startActivity(intent)
        }
    }
}