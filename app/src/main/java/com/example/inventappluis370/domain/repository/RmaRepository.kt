package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.RMA

/**
 * Interfaz del repositorio para las operaciones del modulo de RMA.
 */
interface RmaRepository {

    suspend fun getRmas(): Result<List<RMA>>

    suspend fun deleteRma(id: String): Result<Unit>

    /**
     * (PENDIENTE) Modo paginado (contrato dual-mode).
     */
    // fun getRmasPaged(perPage: Int = 25): Flow<PagingData<RMA>>
}

