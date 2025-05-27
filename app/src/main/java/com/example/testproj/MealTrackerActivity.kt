package com.example.testproj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.TextView
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
    private lateinit var caloriesText: TextView
    private lateinit var macroText: TextView
    private lateinit var progressBar: ProgressBar

    private var selectedDate: String = ""
    private var userTDEE: Int = 2300 // fallback in case we can't load it

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_journal)

        // UI elements
        mealRecyclerView = findViewById(R.id.recyclerViewMeals)
        calendarView = findViewById(R.id.calendarView)
        caloriesText = findViewById(R.id.textViewCalories)
        macroText = findViewById(R.id.textViewMacros)
        progressBar = findViewById(R.id.progressBarCalories)

        mealRecyclerView.layoutManager = LinearLayoutManager(this)

        selectedDate = getCurrentDate()
        fetchUserTDEE() // will call fetchMealsForDate internally after loading TDEE

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = formatDate(year, month, dayOfMonth)
            Log.d("MealTrackerActivity", "Selected date: $selectedDate")
            fetchMealsForDate(selectedDate)
        }

        findViewById<Button>(R.id.buttonAddMeal).setOnClickListener {
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

    private fun fetchUserTDEE() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/tdee")

        ref.get().addOnSuccessListener { snapshot ->
            val tdeeValue = snapshot.getValue(Int::class.java)
            if (tdeeValue != null) {
                userTDEE = tdeeValue
            }
            progressBar.max = userTDEE
            fetchMealsForDate(selectedDate) // moved here after TDEE is guaranteed
        }.addOnFailureListener {
            progressBar.max = userTDEE
            fetchMealsForDate(selectedDate) // fallback
        }
    }

    private fun fetchMealsForDate(date: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("meals").child(uid).child(date)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mealsForDate = mutableListOf<Meal>()
                var totalCalories = 0
                var totalProtein = 0
                var totalCarbs = 0
                var totalFats = 0

                for (mealSnapshot in snapshot.children) {
                    val meal = mealSnapshot.getValue(Meal::class.java)
                    if (meal != null) {
                        mealsForDate.add(meal)
                        totalCalories += meal.calories ?: 0
                        totalProtein += meal.protein ?: 0
                        totalCarbs += meal.carbs ?: 0
                        totalFats += meal.fats ?: 0
                    }
                }

                caloriesText.text = "Calories: $totalCalories / $userTDEE kcal"
                macroText.text = "Protein: ${totalProtein}g | Carbs: ${totalCarbs}g | Fats: ${totalFats}g"
                progressBar.progress = totalCalories

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
