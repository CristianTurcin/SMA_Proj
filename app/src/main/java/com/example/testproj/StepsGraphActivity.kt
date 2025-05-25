package com.example.testproj

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
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
    private lateinit var targetText: TextView
    private lateinit var buttonSetTarget: Button

    private var currentTarget = 6000
    private val notificationChannelId = "step_goal_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps_graph)

        barChart = findViewById(R.id.barChart)
        stepsText = findViewById(R.id.textViewStepCount)
        progressBar = findViewById(R.id.stepsProgressBar)
        targetText = findViewById(R.id.textViewTarget)
        buttonSetTarget = findViewById(R.id.buttonSetTarget)

        buttonSetTarget.setOnClickListener {
            showSetTargetDialog()
        }

        createNotificationChannel()
        loadStepGoal()
        loadStepsData()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Step Goal Notifications"
            val description = "Notifies when the daily step goal is reached"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(notificationChannelId, name, importance).apply {
                this.description = description
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendGoalNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(android.R.drawable.star_big_on)
            .setContentTitle("🎉 Congratulations!")
            .setContentText("You've reached your daily step goal! 🏆")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(1002, notification)
    }

    private fun loadStepGoal() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val goalRef = FirebaseDatabase.getInstance().getReference("users/$uid/stepGoal")

        goalRef.get().addOnSuccessListener { snapshot ->
            currentTarget = snapshot.getValue(Int::class.java) ?: 6000
            progressBar.max = currentTarget
            targetText.text = "Target: %,d".format(currentTarget)
        }.addOnFailureListener {
            currentTarget = 6000
            progressBar.max = currentTarget
            targetText.text = "Target: %,d".format(currentTarget)
        }
    }

    private fun showSetTargetDialog() {
        val input = EditText(this).apply {
            hint = "Enter step goal"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle("Set Step Goal")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newTarget = input.text.toString().toIntOrNull()
                if (newTarget != null && newTarget > 0) {
                    saveNewStepGoal(newTarget)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveNewStepGoal(goal: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("users/$uid/stepGoal").setValue(goal)
        currentTarget = goal
        progressBar.max = goal
        targetText.text = "Target: %,d".format(goal)
    }

    private fun loadStepsData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$uid/steps")

        dbRef.get().addOnSuccessListener { snapshot ->
            val entries = ArrayList<BarEntry>()
            val labels = ArrayList<String>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val sortedDates = snapshot.children.mapNotNull { it.key }.sorted()
            var currentSteps = 0
            sortedDates.forEachIndexed { index, date ->
                val steps = snapshot.child(date).getValue(Int::class.java) ?: 0
                entries.add(BarEntry(index.toFloat(), steps.toFloat()))
                labels.add(date.takeLast(2))

                if (date == dateFormat.format(Date())) {
                    currentSteps = steps
                }
            }

            stepsText.text = "$currentSteps steps"
            progressBar.progress = currentSteps

            if (currentSteps >= currentTarget) {
                sendGoalNotification()
            }

            val dataSet = BarDataSet(entries, "Steps").apply {
                color = android.graphics.Color.CYAN
                valueTextColor = android.graphics.Color.WHITE
                valueTextSize = 10f
                barShadowColor = android.graphics.Color.DKGRAY
            }

            val barData = BarData(dataSet).apply {
                barWidth = 0.35f
            }

            barChart.apply {
                data = barData
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
                axisLeft.axisMaximum = currentTarget.toFloat()
                description.isEnabled = false
                legend.isEnabled = false
                setFitBars(true)
                invalidate()
            }
        }
    }
}
