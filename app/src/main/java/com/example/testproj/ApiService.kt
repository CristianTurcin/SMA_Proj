package com.example.testproj

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Headers

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/predict")
    fun sendData(@Body request: WeightRequest): Call<WeightResponse>
}