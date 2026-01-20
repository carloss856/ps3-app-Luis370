package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.PropiedadEquipo
import com.example.inventappluis370.data.model.PropiedadEquipoRequest

/**
 * Interfaz del repositorio para las operaciones del módulo de Propiedad de Equipos.
 */
interface PropiedadEquipoRepository {
    suspend fun getPropiedades(): Result<List<PropiedadEquipo>>
    suspend fun getByEquipo(equipoId: String): Result<PropiedadEquipo>
    suspend fun createPropiedad(request: PropiedadEquipoRequest): Result<Unit>
    suspend fun updatePropiedad(id: String, request: PropiedadEquipoRequest): Result<Unit>
    suspend fun deletePropiedad(id: String): Result<Unit>

    /**
     * (PENDIENTE) Modo paginado (contrato dual-mode).
     */
    // fun getPropiedadesPaged(perPage: Int = 25): Flow<PagingData<PropiedadEquipo>>
}
