package com.example.testproj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExerciseAdapter(private val exerciseList: List<Exercise>) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exerciseList[position]
        holder.nameTextView.text = exercise.name
        holder.repetitionsTextView.text = "Repetitions: ${exercise.reps}"
        holder.setsTextView.text = "Sets: ${exercise.sets}"
        holder.weightTextView.text = "Weight: ${exercise.weight} kg" // Adăugat pentru a arăta greutatea
    }

    override fun getItemCount(): Int {
        return exerciseList.size
    }

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
        val repetitionsTextView: TextView = itemView.findViewById(R.id.textViewRepetitions)
        val setsTextView: TextView = itemView.findViewById(R.id.textViewSets)
        val weightTextView: TextView = itemView.findViewById(R.id.textViewWeight) // TextView pentru greutate
    }
}
