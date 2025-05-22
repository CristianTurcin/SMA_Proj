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

class AddExerciseActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_exercise)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance().getReference("exercises").child(uid)

        val exerciseNameEditText: EditText = findViewById(R.id.editTextExerciseName)
        val setsEditText: EditText = findViewById(R.id.editTextSets)
        val repsEditText: EditText = findViewById(R.id.editTextReps)
        val weightEditText: EditText = findViewById(R.id.editTextWeight)
        val saveButton: Button = findViewById(R.id.buttonSaveExercise)

        saveButton.setOnClickListener {
            val exerciseName = exerciseNameEditText.text.toString()
            val sets = setsEditText.text.toString()
            val reps = repsEditText.text.toString()
            val weight = weightEditText.text.toString()

            if (exerciseName.isEmpty() || sets.isEmpty() || reps.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val exerciseId = database.push().key ?: return@setOnClickListener
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val exercise = Exercise(exerciseId, exerciseName, sets.toInt(), reps.toInt(), weight.toDouble(), date)


            database.child(date).child(exerciseId).setValue(exercise)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Exercise saved successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to save exercise", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
