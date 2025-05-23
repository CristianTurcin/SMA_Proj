package com.example.testproj

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class WeightChartActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_chart)

        lineChart = findViewById(R.id.lineChart)
        loadWeightHistory()
    }

    private fun setupChart(lineChart: LineChart, entries: List<Entry>, labels: List<String>) {
        val dataSet = LineDataSet(entries, "Weight (kg)").apply {
            color = Color.CYAN
            setCircleColor(Color.CYAN)
            valueTextColor = Color.WHITE
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
        }

        lineChart.data = LineData(dataSet)

        // Background
        lineChart.setBackgroundColor(Color.parseColor("#121212"))

        // Grid lines
        lineChart.axisLeft.gridColor = Color.parseColor("#333333")
        lineChart.axisRight.gridColor = Color.parseColor("#333333")
        lineChart.xAxis.gridColor = Color.parseColor("#333333")

        // Text colors
        lineChart.axisLeft.textColor = Color.WHITE
        lineChart.axisRight.textColor = Color.WHITE
        lineChart.xAxis.textColor = Color.WHITE
        lineChart.legend.textColor = Color.WHITE
        lineChart.description.textColor = Color.WHITE

        // Disable right axis
        lineChart.axisRight.isEnabled = false

        // X axis labels
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            labelRotationAngle = -45f
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(labels)
            textColor = Color.WHITE
        }

        lineChart.invalidate()
    }

    private fun loadWeightHistory() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("weightHistory")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = ArrayList<Entry>()
                val labels = ArrayList<String>()
                val sortedDates = snapshot.children.mapNotNull { it.key }.sorted()

                sortedDates.forEachIndexed { index, date ->
                    val weight = snapshot.child(date).getValue(Float::class.java) ?: return@forEachIndexed
                    entries.add(Entry(index.toFloat(), weight))
                    labels.add(date)
                }

                setupChart(lineChart, entries, labels)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@WeightChartActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
