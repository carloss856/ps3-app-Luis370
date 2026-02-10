package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.domain.model.DashboardData

interface DashboardRepository {
    suspend fun getDashboard(): DashboardData
}
