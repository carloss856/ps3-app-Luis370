package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.model.Notificacion
import com.example.inventappluis370.data.remote.NotificacionApiService
import com.example.inventappluis370.domain.repository.NotificacionRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificacionRepositoryImpl @Inject constructor(
    private val apiService: NotificacionApiService
) : NotificacionRepository {

    override suspend fun getNotificaciones(): Result<List<Notificacion>> {
        return try {
            val response = apiService.getNotificaciones()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException("Error al obtener las notificaciones: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotificacion(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteNotificacion(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Error al eliminar la notificación: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // PENDIENTE: Paging Notificaciones (dual-mode)
    // fun getNotificacionesPaged(perPage: Int): Flow<PagingData<Notificacion>> { ... }
}
