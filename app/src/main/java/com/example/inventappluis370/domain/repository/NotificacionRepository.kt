package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.Notificacion

/**
 * Interfaz del repositorio para las operaciones del módulo de Notificaciones.
 */
interface NotificacionRepository {

    suspend fun getNotificaciones(): Result<List<Notificacion>>

    suspend fun deleteNotificacion(id: String): Result<Unit>

    /**
     * (PENDIENTE) Modo paginado (contrato dual-mode).
     */
    // fun getNotificacionesPaged(perPage: Int = 25): Flow<PagingData<Notificacion>>
}
