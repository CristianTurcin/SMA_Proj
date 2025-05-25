package com.example.testproj

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class StepsGraphActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var stepsText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps_graph)

        barChart = findViewById(R.id.barChart)
        stepsText = findViewById(R.id.textViewStepCount)
        progressBar = findViewById(R.id.stepsProgressBar)

        loadStepsData()
    }

    private fun loadStepsData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$uid/steps")

        dbRef.get().addOnSuccessListener { snapshot ->
            val entries = ArrayList<BarEntry>()
            val labels = ArrayList<String>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val labelFormat = SimpleDateFormat("d", Locale.getDefault())

            val sortedDates = snapshot.children.mapNotNull { it.key }.sorted()
            var currentSteps = 0
            sortedDates.forEachIndexed { index, date ->
                val steps = snapshot.child(date).getValue(Int::class.java) ?: 0
                entries.add(BarEntry(index.toFloat(), steps.toFloat()))
                labels.add(date.takeLast(2))

                // verificăm dacă este azi
                if (date == dateFormat.format(Date())) {
                    currentSteps = steps
                }
            }

            stepsText.text = "$currentSteps steps"
            progressBar.max = 6000
            progressBar.progress = currentSteps

            val dataSet = BarDataSet(entries, "Steps")
            val barData = BarData(dataSet)
            barData.barWidth = 0.35f

            barChart.apply {
                data = barData
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
                axisLeft.axisMaximum = 6000f
                description.isEnabled = false
                legend.isEnabled = false
                setFitBars(true)
                invalidate()
            }
        }
    }
}
