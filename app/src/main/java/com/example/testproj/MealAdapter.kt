package com.example.testproj

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MealAdapter(private val mealList: List<Meal>) :
    RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = mealList[position]
        Log.d("MealAdapter", "Binding meal: ${meal.name}, date: ${meal.date}")
        holder.nameTextView.text = meal.name
        holder.caloriesTextView.text = "Calories: ${meal.calories} kcal"
        holder.proteinTextView.text = "Protein: ${meal.protein} g"
        holder.carbsTextView.text = "Carbs: ${meal.carbs} g"
        holder.fatsTextView.text = "Fats: ${meal.fats} g"
        holder.dateTextView.text = "Date: ${meal.date}"
    }

    override fun getItemCount(): Int = mealList.size

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewMealName)
        val caloriesTextView: TextView = itemView.findViewById(R.id.textViewCalories)
        val proteinTextView: TextView = itemView.findViewById(R.id.textViewProtein)
        val carbsTextView: TextView = itemView.findViewById(R.id.textViewCarbs)
        val fatsTextView: TextView = itemView.findViewById(R.id.textViewFats)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewMealDate)
    }
}