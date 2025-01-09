package com.example.testproj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class StepsAdapter(private val stepsList: List<StepsData>) :
    RecyclerView.Adapter<StepsAdapter.StepsViewHolder>() {

    class StepsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        val stepsTextView: TextView = itemView.findViewById(R.id.textViewSteps)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_step, parent, false)
        return StepsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StepsViewHolder, position: Int) {
        val currentStepData = stepsList[position]
        holder.dateTextView.text = currentStepData.date
        holder.stepsTextView.text = currentStepData.steps.toString()
    }

    override fun getItemCount(): Int {
        return stepsList.size
    }

    fun updateSteps(newStepsList: List<StepsData>) {
        (stepsList as ArrayList).clear()
        stepsList.addAll(newStepsList)
        notifyDataSetChanged()
    }
}
