package com.plantecare.appmobile.api

import com.plantecare.appmobile.models.LoginRequest
import com.plantecare.appmobile.models.LoginResponse
import com.plantecare.appmobile.models.PotResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("api/pot/me")
    fun getPotsByUser(@Header("Authorization") token: String): Call<List<PotResponse>>

    @GET("api/pot/admin")
    fun getAllPots(@Header("Authorization") token: String): Call<List<PotResponse>>
}