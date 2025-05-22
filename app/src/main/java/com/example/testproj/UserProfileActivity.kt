package com.example.testproj

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class UserProfileActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editAge: EditText
    private lateinit var editHeight: EditText
    private lateinit var editWeight: EditText
    private lateinit var spinnerSex: Spinner
    private lateinit var spinnerGoal: Spinner
    private lateinit var spinnerActivityLevel: Spinner
    private lateinit var buttonSave: Button
    private lateinit var textLastUpdated: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Link UI components
        editName = findViewById(R.id.editName)
        editAge = findViewById(R.id.editAge)
        editHeight = findViewById(R.id.editHeight)
        editWeight = findViewById(R.id.editWeight)
        spinnerSex = findViewById(R.id.spinnerSex)
        spinnerGoal = findViewById(R.id.spinnerGoal)
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel)
        buttonSave = findViewById(R.id.buttonSaveProfile)
        textLastUpdated = findViewById(R.id.textLastUpdated)

        // Set spinner options
        val sexOptions = arrayOf("Male", "Female")
        val goalOptions = arrayOf("Lose weight", "Maintain", "Gain muscle")
        val activityOptions = arrayOf(
            "Sedentary (< 5,000 steps/day)",
            "Light (5,000 – 7,499 steps/day)",
            "Moderate (7,500 – 9,999 steps/day)",
            "Intense (10,000 – 12,499 steps/day)",
            "Very intense (12,500+ steps/day)"
        )

        spinnerSex.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sexOptions)
        spinnerGoal.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, goalOptions)
        spinnerActivityLevel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, activityOptions)

        buttonSave.setOnClickListener {
            saveUserProfile()
        }

        loadUserProfile()

        findViewById<ImageButton>(R.id.buttonInfoBMI).setOnClickListener {
            val content = """
                Body Mass Index (BMI) represents the ratio between weight and height squared.
                Formula: BMI = Weight (kg) / (Height in meters)²

                Below 18.5 – Underweight
                18.5–24.9 – Normal weight
                25–29.9 – Overweight
                30+ – Obese
            """.trimIndent()

            InfoBottomSheet("What is BMI?", content).show(supportFragmentManager, "info_bmi")
        }

        findViewById<ImageButton>(R.id.buttonInfoBMR).setOnClickListener {
            val content = """
                BMR (Basal Metabolic Rate) is the amount of energy burned while at rest.
                It is calculated based on sex, weight, height, and age.

                Represents the base daily caloric need.
            """.trimIndent()

            InfoBottomSheet("What is BMR?", content).show(supportFragmentManager, "info_bmr")
        }

        findViewById<ImageButton>(R.id.buttonInfoTDEE).setOnClickListener {
            val content = """
                TDEE (Total Daily Energy Expenditure) represents the total daily caloric requirement.
                Formula: TDEE = BMR × Activity Factor

                It is used to maintain, lose, or gain weight.
            """.trimIndent()

            InfoBottomSheet("What is TDEE?", content).show(supportFragmentManager, "info_tdee")
        }
    }

    private fun saveUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().getReference("users").child(uid)

        val name = editName.text.toString()
        val age = editAge.text.toString().toIntOrNull()
        val height = editHeight.text.toString().toIntOrNull()
        val weight = editWeight.text.toString().toFloatOrNull()

        when {
            name.isBlank() -> {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return
            }
            age == null || age <= 0 || age > 120 -> {
                Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
                return
            }
            height == null || height <= 0 || height > 300 -> {
                Toast.makeText(this, "Please enter a valid height", Toast.LENGTH_SHORT).show()
                return
            }
            weight == null || weight <= 0 || weight > 600 -> {
                Toast.makeText(this, "Please enter a valid weight", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        val userProfile = mapOf(
            "name" to name,
            "age" to age,
            "height" to height,
            "weight" to weight,
            "sex" to spinnerSex.selectedItem.toString(),
            "goal" to spinnerGoal.selectedItem.toString(),
            "activityLevel" to spinnerActivityLevel.selectedItem.toString(),
            "lastUpdated" to currentTime
        )

        db.setValue(userProfile).addOnSuccessListener {
            Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                editName.setText(snapshot.child("name").value?.toString() ?: "")
                editAge.setText(snapshot.child("age").value?.toString() ?: "")
                editHeight.setText(snapshot.child("height").value?.toString() ?: "")
                editWeight.setText(snapshot.child("weight").value?.toString() ?: "")

                val sex = snapshot.child("sex").value?.toString()
                val goal = snapshot.child("goal").value?.toString()
                val activityLevel = snapshot.child("activityLevel").value?.toString()
                val lastUpdated = snapshot.child("lastUpdated").value?.toString()

                sex?.let { setSpinnerSelection(spinnerSex, it) }
                goal?.let { setSpinnerSelection(spinnerGoal, it) }
                activityLevel?.let { setSpinnerSelection(spinnerActivityLevel, it) }

                textLastUpdated.text = "Last updated: ${lastUpdated ?: "unknown"}"

                // Calculate and display BMI, BMR, TDEE
                val weight = snapshot.child("weight").value?.toString()?.toFloatOrNull()
                val height = snapshot.child("height").value?.toString()?.toIntOrNull()
                val age = snapshot.child("age").value?.toString()?.toIntOrNull()
                val sexValue = snapshot.child("sex").value?.toString() ?: "Male"
                val activityLevelValue = snapshot.child("activityLevel").value?.toString() ?: "Sedentary"

                if (weight != null && height != null && age != null) {
                    val heightM = height / 100f
                    val bmi = weight / (heightM * heightM)

                    val bmiStatus = when {
                        bmi < 18.5 -> "Underweight"
                        bmi < 25 -> "Normal weight"
                        bmi < 30 -> "Overweight"
                        bmi < 35 -> "Obese (Class I)"
                        bmi < 40 -> "Obese (Class II)"
                        else -> "Morbid obesity"
                    }

                    findViewById<TextView>(R.id.textBMIResult).text = "$bmiStatus - %.2f kg/m2".format(bmi)

                    val bmr = if (sexValue.lowercase() == "male") {
                        10 * weight + 6.25 * height - 5 * age + 5
                    } else {
                        10 * weight + 6.25 * height - 5 * age - 161
                    }

                    findViewById<TextView>(R.id.textBMRResult).text = "${bmr.toInt()} kCal"

                    val activityFactor = when {
                        activityLevelValue.contains("Sedentary", true) -> 1.2
                        activityLevelValue.contains("Light", true) -> 1.375
                        activityLevelValue.contains("Moderate", true) -> 1.55
                        activityLevelValue.contains("Intense", true) -> 1.725
                        activityLevelValue.contains("Very", true) -> 1.9
                        else -> 1.2
                    }

                    val tdee = bmr * activityFactor
                    findViewById<TextView>(R.id.textTDEEResult).text = "${tdee.toInt()} kCal"
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString().equals(value, ignoreCase = true)) {
                spinner.setSelection(i)
                break
            }
        }
    }
}
