package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.Repuesto
import com.example.inventappluis370.data.model.RepuestoRequest

/**
 * Interfaz del repositorio para las operaciones CRUD del módulo de Repuestos.
 */
interface RepuestoRepository {

    suspend fun getRepuestos(): Result<List<Repuesto>>

    suspend fun createRepuesto(repuestoRequest: RepuestoRequest): Result<Unit>

    suspend fun updateRepuesto(id: String, repuestoRequest: RepuestoRequest): Result<Unit>

    suspend fun deleteRepuesto(id: String): Result<Unit>

    /**
     * (PENDIENTE) Modo paginado (contrato dual-mode).
     */
    // fun getRepuestosPaged(perPage: Int = 25): Flow<PagingData<Repuesto>>
}
