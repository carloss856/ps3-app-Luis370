package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.Garantia
import com.example.inventappluis370.data.model.GarantiaRequest

/**
 * Interfaz del repositorio para las operaciones del módulo de Garantías.
 */
interface GarantiaRepository {
    suspend fun getGarantias(): Result<List<Garantia>>
    suspend fun getGarantiaById(id: String): Result<Garantia>
    suspend fun createGarantia(request: GarantiaRequest): Result<Unit>
    suspend fun updateGarantia(id: String, request: GarantiaRequest): Result<Unit>
    suspend fun deleteGarantia(id: String): Result<Unit>

    /**
     * (PENDIENTE) Modo paginado (contrato dual-mode).
     */
    // fun getGarantiasPaged(perPage: Int = 25): Flow<PagingData<Garantia>>
}
