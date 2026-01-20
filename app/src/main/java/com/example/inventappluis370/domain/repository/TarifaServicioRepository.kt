package com.example.inventappluis370.domain.repository

import androidx.paging.PagingData
import com.example.inventappluis370.data.common.ApiResult
import com.example.inventappluis370.data.model.CreateTarifaRequest
import com.example.inventappluis370.data.model.TarifaServicio
import com.example.inventappluis370.data.model.UpdateTarifaRequest
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para las operaciones del módulo de Tarifas de Servicio.
 */
interface TarifaServicioRepository {
    suspend fun getTarifas(): Result<List<TarifaServicio>>
    suspend fun createTarifa(request: CreateTarifaRequest): Result<Unit>
    suspend fun updateTarifa(id: String, request: UpdateTarifaRequest): Result<Unit>

    /**
     * Versiones ricas (ApiResult) para manejar 422 con errores por campo.
     */
    suspend fun createTarifaResult(request: CreateTarifaRequest): ApiResult<Unit>
    suspend fun updateTarifaResult(id: String, request: UpdateTarifaRequest): ApiResult<Unit>

    suspend fun deleteTarifa(id: String): Result<Unit>
    suspend fun getHistorialTarifa(id: String): Result<List<TarifaServicio>>

    /**
     * Modo paginado (contrato dual-mode): usa query params page/per_page y recibe wrapper {data, meta}.
     */
    fun getTarifasPaged(perPage: Int = 25): Flow<PagingData<TarifaServicio>>
}
