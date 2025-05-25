package com.example.testproj

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null

    private lateinit var textStepToday: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        scheduleDailyReminder()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().getReference("users").child(uid)

        val textWelcome = findViewById<TextView>(R.id.textWelcome)
        val textMotivation = findViewById<TextView>(R.id.textMotivation)
        textStepToday = findViewById(R.id.textStepToday)

        db.child("name").get().addOnSuccessListener { snapshot ->
            val name = snapshot.value?.toString() ?: "user"
            textWelcome.text = "Welcome, $name! 👋"
            textMotivation.text = "\"Keep pushing! Every step counts.\""
        }.addOnFailureListener {
            textWelcome.text = "Welcome! 👋"
            textMotivation.text = "\"Stay consistent, results will follow.\""
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        val buttonExercise: Button = findViewById(R.id.buttonExercise)
        val buttonMeals: Button = findViewById(R.id.buttonMeals)
        val buttonSteps: Button = findViewById(R.id.buttonSteps)

        findViewById<Button>(R.id.buttonUserProfile).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        buttonExercise.setOnClickListener {
            startActivity(Intent(this, ExerciseJournalActivity::class.java))
        }

        buttonMeals.setOnClickListener {
            startActivity(Intent(this, MealTrackerActivity::class.java))
        }

        buttonSteps.setOnClickListener {
            startActivity(Intent(this, StepsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        stepCounterSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val currentRaw = event.values[0]
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val db = FirebaseDatabase.getInstance().getReference("users").child(uid)

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

            db.child("stepsRaw").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    val yesterdayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterday.time)

                    val yesterdaySteps = snapshot.child(yesterdayKey).getValue(Float::class.java)

                    db.child("stepsRaw").child(today).setValue(currentRaw)

                    if (yesterdaySteps != null) {
                        val todaySteps = (currentRaw - yesterdaySteps).toInt()
                        db.child("steps").child(today).setValue(todaySteps)
                        textStepToday.text = "Today you walked: $todaySteps steps"
                    } else {
                        // Nu avem referință pentru ieri -> nu putem calcula azi
                        textStepToday.text = "Today you walked: -- steps"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    textStepToday.text = "Steps data not available"
                }
            })
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun scheduleDailyReminder() {
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 16)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}
