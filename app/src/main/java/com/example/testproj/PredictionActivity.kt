package com.example.testproj

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PredictionActivity : AppCompatActivity() {

    private lateinit var textPredictions: TextView
    private lateinit var database: DatabaseReference
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)

        textPredictions = findViewById(R.id.textPredictions)

        uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance().reference

        sendWeightRequest()
    }

    private fun sendWeightRequest() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val daysData = mutableListOf<List<Float>>()

        fun collectDataForDay(date: String, onComplete: () -> Unit) {
            Log.d("ML_FETCH", "Checking date: $date")
            val weightRef = database.child("users").child(uid).child("weightHistory").child(date)
            val stepsRef = database.child("users").child(uid).child("steps").child(date)
            val mealsRef = database.child("meals").child(uid).child(date)


            var weight: Float? = null
            var steps: Float? = null
            var calories: Float? = null
            var count = 0

            fun tryFinish() {
                count++
                if (count == 3) {
                    if (weight != null && calories != null && steps != null) {
                        Log.d("ML_FILTER", "✅ Accepted $date | weight=$weight, cal=$calories, steps=$steps")
                        daysData.add(listOf(weight!!, calories!!, steps!!))
                    } else {
                        Log.d("ML_FILTER", "❌ Skipped $date | weight=$weight, cal=$calories, steps=$steps")
                    }
                    onComplete()
                }
            }

            weightRef.get().addOnSuccessListener {
                weight = it.getValue(Float::class.java)
                Log.d("ML_FETCH", "Weight for $date: $weight")
                tryFinish()
            }.addOnFailureListener {
                Log.e("ML_ERROR", "Failed to fetch weight for $date", it)
                tryFinish()
            }

            stepsRef.get().addOnSuccessListener {
                steps = it.getValue(Long::class.java)?.toFloat()
                Log.d("ML_FETCH", "Steps for $date: $steps")
                tryFinish()
            }.addOnFailureListener {
                Log.e("ML_ERROR", "Failed to fetch steps for $date", it)
                tryFinish()
            }

            mealsRef.get().addOnSuccessListener {
                var totalCalories = 0f
                for (mealSnapshot in it.children) {
                    val cal = mealSnapshot.child("calories").getValue(Long::class.java)
                    if (cal != null) totalCalories += cal.toFloat()
                }
                calories = if (totalCalories > 0) totalCalories else null
                Log.d("ML_FETCH", "Calories for $date: $calories")
                tryFinish()
            }.addOnFailureListener {
                Log.e("ML_ERROR", "Failed to fetch meals for $date", it)
                tryFinish()
            }
        }

        fun collectDays(daysRemaining: Int) {
            if (daysRemaining == 0 || daysData.size == 14) {
                if (daysData.size == 14) {
                    Log.d("ML_SUMMARY", "Collected 14 complete days. Sending request.")
                    sendRequestToML(daysData)
                } else {
                    Log.d("ML_SUMMARY", "Not enough data. Only ${daysData.size} days collected.")
                    textPredictions.text = "Not enough complete days found (needed 14). Found ${daysData.size}."
                }
                return
            }
            val dateStr = dateFormat.format(calendar.time)
            collectDataForDay(dateStr) {
                calendar.add(Calendar.DATE, -1)
                collectDays(daysRemaining - 1)
            }
        }

        collectDays(50)
    }

    private fun sendRequestToML(data: List<List<Float>>) {
        Log.d("ML_REQUEST", "Sending data: $data")
        val request = WeightRequest(sequence = data.takeLast(14))
        val call = RetrofitClient.instance.sendData(request)

        call.enqueue(object : Callback<WeightResponse> {
            override fun onResponse(call: Call<WeightResponse>, response: Response<WeightResponse>) {
                if (response.isSuccessful) {
                    val results = response.body()?.predictedWeights
                    Log.d("ML_RESPONSE", "Received: $results")
                    textPredictions.text = "Predicted weights:\n${results?.joinToString("\n")}"
                    if (results != null) showChart(results)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ML_RESPONSE", "Server error: $errorBody")
                    textPredictions.text = "Server error: $errorBody"
                }
            }

            override fun onFailure(call: Call<WeightResponse>, t: Throwable) {
                Log.e("ML_RESPONSE", "Network error: ${t.message}", t)
                textPredictions.text = "Network error: ${t.message}"
            }
        })
    }

    private fun showChart(predictions: List<Float>) {
        val chart = findViewById<LineChart>(R.id.predictionChart)
        val entries = predictions.mapIndexed { index, value -> Entry(index.toFloat(), value) }

        val dataSet = LineDataSet(entries, "Predicted Weights").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setCircleColor(Color.CYAN)
            circleRadius = 4f
        }

        chart.data = LineData(dataSet)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.description.text = "Next 7 days"
        chart.invalidate()
    }
}
