package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.model.Reporte
import com.example.inventappluis370.data.model.ReporteRequest
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.ReporteApiService
import com.example.inventappluis370.domain.repository.ReporteRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReporteRepositoryImpl @Inject constructor(
    private val apiService: ReporteApiService
) : ReporteRepository {

    override suspend fun getReportes(): Result<List<Reporte>> {
        return try {
            val response = apiService.getReportes()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createReporte(reporteRequest: ReporteRequest): Result<Unit> {
        return try {
            val response = apiService.createReporte(reporteRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReporte(id: String, reporteRequest: ReporteRequest): Result<Unit> {
        return try {
            val response = apiService.updateReporte(id, reporteRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReporte(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteReporte(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // PENDIENTE: Paging Reportes (dual-mode)
    // fun getReportesPaged(perPage: Int): Flow<PagingData<Reporte>> { ... }
}
