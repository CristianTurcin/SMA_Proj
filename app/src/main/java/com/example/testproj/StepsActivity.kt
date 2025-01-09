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

class StepsActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private lateinit var stepsTextView: TextView

    private var isSensorRegistered = false
    private var initialStepCount: Int = -1 // Pentru a calcula pașii incrementali

    // TAG pentru loguri
    private val TAG = "StepsActivity"
    private val REQUEST_CODE = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps)

        stepsTextView = findViewById(R.id.textViewSteps)

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

            if (initialStepCount.toFloat() == -1f) {
                initialStepCount = event.values[0].toInt() // Setăm numărul inițial de pași
                Log.d(TAG, "Initial step count set to: $initialStepCount")
            }

            val totalSteps = event.values[0]
            val stepsSinceStart = totalSteps - initialStepCount
            Log.d(TAG, "Total steps: $totalSteps, Steps since activity started: $stepsSinceStart")

            stepsTextView.text = "Steps: ${stepsSinceStart.toInt()}"
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

        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisiunea a fost acordată, poți accesa senzorul
                Log.d(TAG, "Permission granted for activity recognition")
            } else {
                // Permisiunea a fost refuzată
                Log.d(TAG, "Permission denied for activity recognition")
            }
        }
    }
}
