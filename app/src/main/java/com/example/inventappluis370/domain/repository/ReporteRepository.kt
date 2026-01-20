package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.Reporte
import com.example.inventappluis370.data.model.ReporteRequest

/**
 * Interfaz del repositorio para las operaciones del módulo de Reportes.
 */
interface ReporteRepository {

    suspend fun getReportes(): Result<List<Reporte>>

    suspend fun createReporte(reporteRequest: ReporteRequest): Result<Unit>

    suspend fun updateReporte(id: String, reporteRequest: ReporteRequest): Result<Unit>

    suspend fun deleteReporte(id: String): Result<Unit>

    /**
     * (PENDIENTE) Modo paginado (contrato dual-mode).
     */
    // fun getReportesPaged(perPage: Int = 25): Flow<PagingData<Reporte>>
}
