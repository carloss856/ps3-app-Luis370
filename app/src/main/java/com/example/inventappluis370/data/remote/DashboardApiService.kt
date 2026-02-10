package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.remote.dto.DashboardResponseDto
import retrofit2.http.GET

interface DashboardApiService {
    @GET("dashboard")
    suspend fun getDashboard(): DashboardResponseDto
}
