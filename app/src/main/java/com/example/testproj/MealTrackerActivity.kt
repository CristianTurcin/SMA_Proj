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

class MealTrackerActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var mealRecyclerView: RecyclerView
    private lateinit var mealAdapter: MealAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_journal)

        // Inițializare RecyclerView și CalendarView
        mealRecyclerView = findViewById(R.id.recyclerViewMeals)
        calendarView = findViewById(R.id.calendarView)

        mealRecyclerView.layoutManager = LinearLayoutManager(this)

        // Afișează mesele pentru data curentă
        fetchMealsForDate(getCurrentDate())

        // Setăm listener-ul pentru calendar
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = formatDate(year, month, dayOfMonth)
            Log.d("MealTrackerActivity", "Selected date: $selectedDate")
            fetchMealsForDate(selectedDate)
        }

        // Buton "Add Meal"
        val addMealButton: Button = findViewById(R.id.buttonAddMeal)
        addMealButton.setOnClickListener {
            startActivity(Intent(this, AddMealActivity::class.java))
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

    private fun fetchMealsForDate(date: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("meals").child(uid).child(date)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mealsForDate = mutableListOf<Meal>()
                for (mealSnapshot in snapshot.children) {
                    val meal = mealSnapshot.getValue(Meal::class.java)
                    if (meal != null) {
                        mealsForDate.add(meal)
                    }
                }

                mealAdapter = MealAdapter(mealsForDate)
                mealRecyclerView.adapter = mealAdapter

                if (mealsForDate.isEmpty()) {
                    Toast.makeText(this@MealTrackerActivity, "No meals for this date.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MealTrackerActivity, "Failed to load meals.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
