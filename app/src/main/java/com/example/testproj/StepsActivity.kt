package com.example.testproj

import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Button
import androidx.core.app.NotificationCompat



class StepsActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private lateinit var stepsTextView: TextView
    private lateinit var endButton: Button

    private var isSensorRegistered = false
    private var initialStepCount: Int = -1 // Pentru a calcula pașii incrementali
    private var totalSteps: Int = 0 // Pașii înregistrați în sesiune

    private val TAG = "StepsActivity"
    private val REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps)

        stepsTextView = findViewById(R.id.textViewSteps)
        endButton = findViewById(R.id.buttonEndStepCounter)

        // Inițializare SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Găsește senzorul de pași
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounter != null) {
            Log.d(TAG, "Step Counter Sensor found: ${stepCounter?.name}")
        } else {
            Log.e(TAG, "No Step Counter Sensor found on this device")
            stepsTextView.text = "Step Counter Sensor not found"
        }

        // Verifică permisiunea pentru recunoașterea activităților
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_CODE
            )
        }

        // Setează funcția pentru butonul "End Step Counter"
        endButton.setOnClickListener {
            Log.d(TAG, "End button clicked. Total steps: $totalSteps")

            // Verificăm permisiunile pentru notificări
            checkNotificationPermissionsAndSend(context = this, steps = totalSteps)

            // Întoarce utilizatorul la DashboardActivity
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)

            // Închide activitatea curentă
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (stepCounter != null) {
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI)
            isSensorRegistered = true
            Log.d(TAG, "Sensor listener registered")
        } else {
            Log.e(TAG, "Cannot register sensor listener, Step Counter Sensor is null")
        }
    }

    override fun onPause() {
        super.onPause()
        if (isSensorRegistered) {
            sensorManager.unregisterListener(this)
            isSensorRegistered = false
            Log.d(TAG, "Sensor listener unregistered")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            Log.d(TAG, "Sensor event received: ${event.sensor.name}, value: ${event.values[0]}")

            if (initialStepCount == -1) {
                initialStepCount = event.values[0].toInt() // Setăm numărul inițial de pași
                Log.d(TAG, "Initial step count set to: $initialStepCount")
            }

            // Calculăm pașii totalizați
            totalSteps = event.values[0].toInt() - initialStepCount
            Log.d(TAG, "Total steps: $totalSteps")

            stepsTextView.text = "Steps: $totalSteps"
        } else {
            Log.e(TAG, "Sensor event is null")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: ${sensor?.name}, new accuracy: $accuracy")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted after request")
                sendNotification(this, totalSteps)
            } else {
                Log.d(TAG, "Notification permission denied after request")
            }
        }
    }


    // Funcția pentru trimiterea notificărilor
    private fun sendNotification(context: Context, steps: Int) {
        Log.d(TAG, "Preparing to send notification with steps: $steps")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "step_counter_notifications"

        // Verifică și creează canalul de notificare dacă este necesar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Step Counter Notifications", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Step Counter Session Ended")
            .setContentText("You walked $steps steps in this session.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        notificationManager.notify("step_counter_notification".hashCode(), notification)
        Log.d(TAG, "Notification sent")
    }

    // Funcția pentru a verifica permisiunea și a trimite notificările
    private fun checkNotificationPermissionsAndSend(context: Context, steps: Int) {
        Log.d(TAG, "Checking notification permission")
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            Log.d(TAG, "Notification permission granted, sending notification...")
            sendNotification(context, steps)
        } else {
            Log.d(TAG, "Notification permission not granted")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    context as? StepsActivity ?: return,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }

}

