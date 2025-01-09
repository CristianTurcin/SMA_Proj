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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MealTrackerActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var mealRecyclerView: RecyclerView
    private lateinit var mealAdapter: MealAdapter
    private lateinit var mealList: MutableList<Meal>
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_journal)

        // Inițializare RecyclerView și CalendarView
        mealRecyclerView = findViewById(R.id.recyclerViewMeals)
        calendarView = findViewById(R.id.calendarView)

        // Setăm RecyclerView
        mealRecyclerView.layoutManager = LinearLayoutManager(this)
        mealList = mutableListOf()
        mealAdapter = MealAdapter(mealList)
        mealRecyclerView.adapter = mealAdapter

        // Referință la Firebase
        database = FirebaseDatabase.getInstance().getReference("meals")

        // Citirea datelor din Firebase
        fetchMealsFromDatabase()

        // Setăm listener-ul pentru calendar
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = formatDate(year, month, dayOfMonth)
            Log.d("MealTrackerActivity", "Selected date: $selectedDate")
            fetchMealsForDate(selectedDate)
        }

        // Adăugăm click listener pentru butonul "Add Meal"
        val addMealButton: Button = findViewById(R.id.buttonAddMeal)
        addMealButton.setOnClickListener {
            // Navighează către AddMealActivity
            val intent = Intent(this, AddMealActivity::class.java)
            startActivity(intent)
        }
    }

    private fun formatDate(year: Int, month: Int, dayOfMonth: Int): String {
        return String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
    }

    // Funcția care citește mesele din Firebase
    private fun fetchMealsFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mealList.clear()
                for (mealSnapshot in snapshot.children) {
                    val meal = mealSnapshot.getValue(Meal::class.java)
                    if (meal != null) {
                        Log.d("MealTrackerActivity", "Fetched meal: ${meal.name}, date: ${meal.date}")
                        mealList.add(meal)
                    }
                }
                mealAdapter.notifyDataSetChanged()

                // Afișează mesele pentru data curentă
                val currentDate = getCurrentDate()
                fetchMealsForDate(currentDate)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Failed to load meals.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Funcția care încarcă mesele pentru o anumită dată
    private fun fetchMealsForDate(date: String) {
        val filteredMeals = mealList.filter {
            val formattedFirebaseDate = formatDateFromFirebase(it.date)
            Log.d("MealTrackerActivity", "Comparing Firebase date: $formattedFirebaseDate with selected date: $date")
            formattedFirebaseDate == date
        }

        Log.d("MealTrackerActivity", "Filtered meals size: ${filteredMeals.size}")

        if (filteredMeals.isNotEmpty()) {
            filteredMeals.forEach { meal ->
                Log.d("MealTrackerActivity", "Filtered meal found: Name: ${meal.name}, Date: ${meal.date}")
            }
            mealAdapter = MealAdapter(filteredMeals)
            mealRecyclerView.adapter = mealAdapter
        } else {
            Toast.makeText(this, "No meals for this date.", Toast.LENGTH_SHORT).show()
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