package com.example.testproj

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AddMealActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meal)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance().getReference("meals").child(uid)

        val mealNameEditText: EditText = findViewById(R.id.editTextMealName)
        val caloriesEditText: EditText = findViewById(R.id.editTextCalories)
        val proteinEditText: EditText = findViewById(R.id.editTextProtein)
        val carbsEditText: EditText = findViewById(R.id.editTextCarbs)
        val fatsEditText: EditText = findViewById(R.id.editTextFats)
        val saveButton: Button = findViewById(R.id.buttonSaveMeal)

        saveButton.setOnClickListener {
            val mealName = mealNameEditText.text.toString()
            val calories = caloriesEditText.text.toString()
            val protein = proteinEditText.text.toString()
            val carbs = carbsEditText.text.toString()
            val fats = fatsEditText.text.toString()

            if (mealName.isEmpty() || calories.isEmpty() || protein.isEmpty() || carbs.isEmpty() || fats.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mealId = database.push().key ?: return@setOnClickListener
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val meal = Meal(mealId, mealName, calories.toInt(), protein.toInt(), carbs.toInt(), fats.toInt(), date)

            database.child(date).child(mealId).setValue(meal)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Meal saved successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to save meal", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
