package com.example.inventappluis370.domain.repository

import androidx.paging.PagingData
import com.example.inventappluis370.data.common.ApiResult
import com.example.inventappluis370.data.model.*
import com.example.inventappluis370.data.remote.PartesResponse
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para las operaciones relacionadas con los Servicios y sus Partes de Trabajo.
 */
interface ServicioRepository {
    suspend fun getServicios(): Result<List<Servicio>>
    suspend fun getServicioById(id: String): Result<Servicio>

    suspend fun createServicio(request: CreateServicioRequest): Result<Unit>
    suspend fun updateServicio(id: String, request: CreateServicioRequest): Result<Unit>

    /** Versiones ricas para manejar 422 con errores por campo. */
    suspend fun createServicioResult(request: CreateServicioRequest): ApiResult<Unit>
    suspend fun updateServicioResult(id: String, request: CreateServicioRequest): ApiResult<Unit>

    suspend fun deleteServicio(id: String): Result<Unit>

    // --- Partes de Trabajo ---
    suspend fun getPartes(servicioId: String): Result<PartesResponse>
    suspend fun createParte(servicioId: String, request: CreateParteTrabajoRequest): Result<Unit>
    suspend fun updateParte(servicioId: String, parteId: String, request: UpdateParteTrabajoRequest): Result<Unit>

    /** Versiones ricas para manejar 422 con errores por campo. */
    suspend fun createParteResult(servicioId: String, request: CreateParteTrabajoRequest): ApiResult<Unit>
    suspend fun updateParteResult(servicioId: String, parteId: String, request: UpdateParteTrabajoRequest): ApiResult<Unit>

    suspend fun deleteParte(servicioId: String, parteId: String): Result<Unit>

    // --- Paginado ---
    /**
     * Modo paginado (contrato dual-mode): usa query params page/per_page y recibe wrapper {data, meta}.
     */
    fun getServiciosPaged(perPage: Int = 25): Flow<PagingData<Servicio>>
}
