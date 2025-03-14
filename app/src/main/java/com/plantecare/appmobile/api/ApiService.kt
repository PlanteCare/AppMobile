package com.plantecare.appmobile.api

import com.plantecare.appmobile.models.LoginRequest
import com.plantecare.appmobile.models.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
}