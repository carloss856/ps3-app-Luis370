package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.CreateSolicitudRequest
import com.example.inventappluis370.data.model.SolicitudRepuesto
import com.example.inventappluis370.data.model.UpdateSolicitudRequest

/**
 * Interfaz del repositorio para las operaciones del módulo de Solicitudes de Repuestos.
 */
interface SolicitudRepuestoRepository {
    suspend fun getSolicitudes(): Result<List<SolicitudRepuesto>>
    suspend fun getSolicitudById(id: String): Result<SolicitudRepuesto>
    suspend fun createSolicitud(request: CreateSolicitudRequest): Result<Unit>
    suspend fun updateSolicitud(id: String, request: UpdateSolicitudRequest): Result<Unit>
    suspend fun deleteSolicitud(id: String): Result<Unit>

    /**
     * (PENDIENTE) Modo paginado (contrato dual-mode).
     */
    // fun getSolicitudesPaged(perPage: Int = 25): Flow<PagingData<SolicitudRepuesto>>
}
