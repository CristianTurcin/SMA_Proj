package com.example.testproj

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class StepsActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private lateinit var stepsTextView: TextView
    private lateinit var endButton: Button
    private lateinit var viewGraphButton: Button

    private var isSensorRegistered = false
    private var initialStepCount: Int = -1
    private var totalSteps: Int = 0

    private val TAG = "StepsActivity"
    private val REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps)

        stepsTextView = findViewById(R.id.textViewSteps)
        endButton = findViewById(R.id.buttonEndStepCounter)
        viewGraphButton = findViewById(R.id.buttonViewStepsGraph)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounter != null) {
            Log.d(TAG, "Step Counter Sensor found: ${stepCounter?.name}")
        } else {
            Log.e(TAG, "No Step Counter Sensor found on this device")
            stepsTextView.text = "Step Counter Sensor not found"
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_CODE
            )
        }

        endButton.setOnClickListener {
            checkNotificationPermissionsAndSend(this, totalSteps)
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        viewGraphButton.setOnClickListener {
            startActivity(Intent(this, StepsGraphActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        stepCounter?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            isSensorRegistered = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (isSensorRegistered) {
            sensorManager.unregisterListener(this)
            isSensorRegistered = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (initialStepCount == -1) {
                initialStepCount = it.values[0].toInt()
            }
            totalSteps = it.values[0].toInt() - initialStepCount
            stepsTextView.text = "Steps: $totalSteps"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendNotification(this, totalSteps)
        }
    }

    private fun sendNotification(context: Context, steps: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "step_counter_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Step Counter Notifications", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Step Counter Session Ended")
            .setContentText("You walked $steps steps in this session.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        notificationManager.notify("step_counter_notification".hashCode(), notification)
    }

    private fun checkNotificationPermissionsAndSend(context: Context, steps: Int) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            sendNotification(context, steps)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                context as? StepsActivity ?: return,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }
}
