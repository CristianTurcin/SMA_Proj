package com.example.testproj

import com.google.gson.annotations.SerializedName

data class WeightResponse(
    @SerializedName("predicted_weights")
    val predictedWeights: List<Float>
)