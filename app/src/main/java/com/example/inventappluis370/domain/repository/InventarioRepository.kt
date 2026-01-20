package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.Inventario
import com.example.inventappluis370.data.model.InventarioRequest

/**
 * Interfaz del repositorio para las operaciones del módulo de Inventario.
 */
interface InventarioRepository {
    suspend fun getInventario(): Result<List<Inventario>>
    suspend fun getInventarioById(id: String): Result<Inventario>
    suspend fun createInventarioEntrada(request: InventarioRequest): Result<Unit>
    suspend fun updateInventarioEntrada(id: String, request: InventarioRequest): Result<Unit>
    suspend fun deleteInventarioEntrada(id: String): Result<Unit>

    /**
     * (PENDIENTE) Modo paginado (contrato dual-mode).
     */
    // fun getInventarioPaged(perPage: Int = 25): Flow<PagingData<Inventario>>
}
