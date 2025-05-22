package com.example.testproj

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserProfileActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editAge: EditText
    private lateinit var editHeight: EditText
    private lateinit var editWeight: EditText
    private lateinit var spinnerSex: Spinner
    private lateinit var spinnerGoal: Spinner
    private lateinit var spinnerActivityLevel: Spinner
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)


        editName = findViewById(R.id.editName)
        editAge = findViewById(R.id.editAge)
        editHeight = findViewById(R.id.editHeight)
        editWeight = findViewById(R.id.editWeight)
        spinnerSex = findViewById(R.id.spinnerSex)
        spinnerGoal = findViewById(R.id.spinnerGoal)
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel)
        buttonSave = findViewById(R.id.buttonSaveProfile)


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
    }

    private fun saveUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().getReference("users").child(uid)

        val userProfile = mapOf(
            "name" to editName.text.toString(),
            "age" to editAge.text.toString().toIntOrNull(),
            "height" to editHeight.text.toString().toIntOrNull(),
            "weight" to editWeight.text.toString().toFloatOrNull(),
            "sex" to spinnerSex.selectedItem.toString(),
            "goal" to spinnerGoal.selectedItem.toString(),
            "activityLevel" to spinnerActivityLevel.selectedItem.toString()
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

                sex?.let { setSpinnerSelection(spinnerSex, it) }
                goal?.let { setSpinnerSelection(spinnerGoal, it) }
                activityLevel?.let { setSpinnerSelection(spinnerActivityLevel, it) }
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
