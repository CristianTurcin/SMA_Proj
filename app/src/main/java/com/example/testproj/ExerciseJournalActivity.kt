package com.example.testproj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ExerciseJournalActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var exerciseRecyclerView: RecyclerView
    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_journal)

        // Inițializare RecyclerView și CalendarView
        exerciseRecyclerView = findViewById(R.id.recyclerViewExercises)
        calendarView = findViewById(R.id.calendarView)

        exerciseRecyclerView.layoutManager = LinearLayoutManager(this)

        // Afișează exercițiile pentru data curentă
        fetchExercisesForDate(getCurrentDate())

        // Listener pentru selectarea unei date în calendar
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = formatDate(year, month, dayOfMonth)
            Log.d("ExerciseJournalActivity", "Selected date: $selectedDate")
            fetchExercisesForDate(selectedDate)
        }

        // Buton "Add Exercise"
        val addExerciseButton: Button = findViewById(R.id.buttonAddExercise)
        addExerciseButton.setOnClickListener {
            startActivity(Intent(this, AddExerciseActivity::class.java))
        }
    }

    private fun formatDate(year: Int, month: Int, dayOfMonth: Int): String {
        return String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun fetchExercisesForDate(date: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("exercises").child(uid).child(date)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val exerciseList = mutableListOf<Exercise>()
                for (exerciseSnapshot in snapshot.children) {
                    val exercise = exerciseSnapshot.getValue(Exercise::class.java)
                    if (exercise != null) {
                        exerciseList.add(exercise)
                    }
                }

                exerciseAdapter = ExerciseAdapter(exerciseList)
                exerciseRecyclerView.adapter = exerciseAdapter

                if (exerciseList.isEmpty()) {
                    Toast.makeText(this@ExerciseJournalActivity, "No exercises for this date.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ExerciseJournalActivity, "Failed to load exercises.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
