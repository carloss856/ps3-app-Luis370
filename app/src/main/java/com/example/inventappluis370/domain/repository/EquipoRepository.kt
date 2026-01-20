package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.Equipo
import com.example.inventappluis370.data.model.EquipoRequest

/**
 * Interfaz del repositorio para las operaciones CRUD del módulo de Equipos.
 */
interface EquipoRepository {

    suspend fun getEquipos(): Result<List<Equipo>>

    // Corregido: Las operaciones de escritura devuelven Unit.
    suspend fun createEquipo(equipoRequest: EquipoRequest): Result<Unit>

    // Corregido: Las operaciones de escritura devuelven Unit.
    suspend fun updateEquipo(id: String, equipoRequest: EquipoRequest): Result<Unit>

    suspend fun deleteEquipo(id: String): Result<Unit>

    /**
     * (PENDIENTE) Modo paginado (contrato dual-mode).
     */
    // fun getEquiposPaged(perPage: Int = 25): Flow<PagingData<Equipo>>
}
