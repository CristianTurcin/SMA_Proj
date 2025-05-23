package com.example.testproj

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)


        scheduleDailyReminder()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().getReference("users").child(uid)

        val textWelcome = findViewById<TextView>(R.id.textWelcome)
        val textMotivation = findViewById<TextView>(R.id.textMotivation)

        db.child("name").get().addOnSuccessListener { snapshot ->
            val name = snapshot.value?.toString() ?: "user"
            textWelcome.text = "Welcome, $name! 👋"
            textMotivation.text = "\"Keep pushing! Every step counts.\""
        }.addOnFailureListener {
            textWelcome.text = "Welcome! 👋"
            textMotivation.text = "\"Stay consistent, results will follow.\""
        }

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
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
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
