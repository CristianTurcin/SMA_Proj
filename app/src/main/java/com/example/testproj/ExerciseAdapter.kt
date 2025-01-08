package com.example.testproj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExerciseAdapter(private val exerciseList: List<Exercise>) :
    RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)  // Asigură-te că ai layout-ul corect
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exerciseList[position]
        holder.nameTextView.text = exercise.name
        holder.setsTextView.text = "Sets: ${exercise.sets}"
        holder.repsTextView.text = "Reps: ${exercise.reps}"
        holder.weightTextView.text = "Weight: ${exercise.weight}"
    }

    override fun getItemCount(): Int = exerciseList.size

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewExerciseName)
        val setsTextView: TextView = itemView.findViewById(R.id.textViewSets)
        val repsTextView: TextView = itemView.findViewById(R.id.textViewReps)
        val weightTextView: TextView = itemView.findViewById(R.id.textViewWeight)
    }
}
