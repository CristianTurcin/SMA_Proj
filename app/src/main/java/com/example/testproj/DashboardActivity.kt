package com.example.testproj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DashboardActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().getReference("users").child(uid)

        val textWelcome = findViewById<TextView>(R.id.textWelcome)
        val textMotivation = findViewById<TextView>(R.id.textMotivation)

        db.child("name").get().addOnSuccessListener { snapshot ->
            val name = snapshot.value?.toString() ?: "user"
            textWelcome.text = "Welcome, $name! 👋"
            textMotivation.text = "\"Keep pushing! Every step counts.\""
        }.addOnFailureListener {
            textWelcome.text = "Welcome! 👋"
            textMotivation.text = "\"Stay consistent, results will follow.\""
        }


        val buttonExercise: Button = findViewById(R.id.buttonExercise)
        val buttonMeals: Button = findViewById(R.id.buttonMeals)
        val buttonSteps: Button = findViewById(R.id.buttonSteps)

        findViewById<Button>(R.id.buttonUserProfile).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }


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

        // Setăm listener pentru butonul de Pași
        buttonSteps.setOnClickListener {
            // Lansează activitatea de Pași
            val intent = Intent(this, StepsActivity::class.java)
            startActivity(intent)
        }

    }
}