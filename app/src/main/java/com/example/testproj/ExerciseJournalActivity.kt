package com.example.testproj

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExerciseJournalActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var exerciseRecyclerView: RecyclerView
    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var exerciseList: MutableList<Exercise>
    private lateinit var filteredExerciseList: MutableList<Exercise>
    private lateinit var database: DatabaseReference
    private lateinit var buttonAddExercise: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_journal)

        // Inițializare RecyclerView și CalendarView
        exerciseRecyclerView = findViewById(R.id.recyclerViewExercises)
        calendarView = findViewById(R.id.calendarView)

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
            val selectedDate = "$year-${month + 1}-$dayOfMonth"  // formatul YYYY-MM-DD
            fetchExercisesForDate(selectedDate)
        }

        // Adăugăm exercițiile din ziua curentă la deschiderea aplicației
        fetchExercisesForDate(getCurrentDate()) // Asigură-te că filtrezi și afișezi exercițiile pentru ziua curentă
    }


    // Funcția care citește exercițiile din Firebase
    private fun fetchExercisesFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
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

                // Afișează exercițiile pentru data curentă la început
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
        val filteredExercises = exerciseList.filter { it.date == date }
        Log.d("ExerciseJournalActivity", "Filtered exercises for date: $date, count: ${filteredExercises.size}")
        exerciseAdapter = ExerciseAdapter(filteredExercises)
        exerciseRecyclerView.adapter = exerciseAdapter
    }

    // Obținem data curentă în formatul YYYY-MM-DD
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}
