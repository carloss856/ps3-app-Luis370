package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.domain.model.ModuleStats
import com.example.inventappluis370.domain.model.StatsPeriod

interface StatsRepository {
    suspend fun getStatsCached(module: String, period: StatsPeriod, from: String? = null, to: String? = null): ModuleStats
}
