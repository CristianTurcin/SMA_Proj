package com.example.testproj

import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExerciseJournalActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var exerciseRecyclerView: RecyclerView
    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var exerciseList: MutableList<Exercise>
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_journal)

        // Inițializare RecyclerView și CalendarView
        exerciseRecyclerView = findViewById(R.id.recyclerViewExercises)
        calendarView = findViewById(R.id.calendarView)

        // Setăm RecyclerView
        exerciseRecyclerView.layoutManager = LinearLayoutManager(this)
        exerciseList = mutableListOf()
        exerciseAdapter = ExerciseAdapter(exerciseList)
        exerciseRecyclerView.adapter = exerciseAdapter

        // Referință la Firebase
        database = FirebaseDatabase.getInstance().getReference("exercises")

        // Citirea datelor din Firebase
        fetchExercisesFromDatabase()

        // Setăm listener-ul pentru calendar
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = formatDate(year, month, dayOfMonth)
            Log.d("ExerciseJournalActivity", "Selected date: $selectedDate")
            fetchExercisesForDate(selectedDate)
        }
    }
    private fun formatDate(year: Int, month: Int, dayOfMonth: Int): String {
        return String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
    }

    // Funcția care citește exercițiile din Firebase
    private fun fetchExercisesFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                exerciseList.clear()
                for (exerciseSnapshot in snapshot.children) {
                    val exercise = exerciseSnapshot.getValue(Exercise::class.java)
                    if (exercise != null) {
                        Log.d("ExerciseJournalActivity", "Fetched exercise: ${exercise.name}, date: ${exercise.date}")
                        exerciseList.add(exercise)
                    }
                }
                exerciseAdapter.notifyDataSetChanged()

                // Afișează exercițiile pentru data curentă
                val currentDate = getCurrentDate()
                fetchExercisesForDate(currentDate)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Failed to load exercises.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Funcția care încarcă exercițiile pentru o anumită dată
    private fun fetchExercisesForDate(date: String) {
        val filteredExercises = exerciseList.filter {
            val formattedFirebaseDate = formatDateFromFirebase(it.date)
            Log.d("ExerciseJournalActivity", "Comparing Firebase date: $formattedFirebaseDate with selected date: $date")
            formattedFirebaseDate == date
        }

        if (filteredExercises.isNotEmpty()) {
            exerciseAdapter = ExerciseAdapter(filteredExercises)
            exerciseRecyclerView.adapter = exerciseAdapter
        } else {
            Toast.makeText(this, "No exercises for this date.", Toast.LENGTH_SHORT).show()
        }
    }

    // Funcția de formatare pentru data din Firebase
    private fun formatDateFromFirebase(firebaseDate: String): String {
        val dateParts = firebaseDate.split("-")
        val year = dateParts[0].toInt()
        val month = dateParts[1].toInt() - 1 // Firebase stochează luna în format 01, 02 etc.
        val day = dateParts[2].toInt()
        return formatDate(year, month, day)
    }

    // Obținem data curentă în formatul YYYY-MM-DD
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}
