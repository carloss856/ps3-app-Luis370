package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.model.Notificacion
import com.example.inventappluis370.data.model.PaginatedResponseDto
import com.example.inventappluis370.data.remote.NotificacionApiService
import com.example.inventappluis370.domain.repository.NotificacionRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificacionRepositoryImpl @Inject constructor(
    private val apiService: NotificacionApiService
) : NotificacionRepository {

    override suspend fun getNotificaciones(): Result<List<Notificacion>> {
        return try {
            // Preferimos RAW para tolerar backends que devuelven vacíos o wrappers.
            val raw = apiService.getNotificacionesRaw()
            if (raw.isSuccessful) {
                val txt = raw.body()?.string()?.trim().orEmpty()
                if (txt.isBlank() || txt == "null") {
                    return Result.success(emptyList())
                }

                val moshi = Moshi.Builder().build()
                val listType = Types.newParameterizedType(List::class.java, Notificacion::class.java)
                val listAdapter = moshi.adapter<List<Notificacion>>(listType)

                // Dual-mode: si empieza con { intentamos wrapper paginado.
                val parsed = if (txt.startsWith("{")) {
                    val pageType = Types.newParameterizedType(PaginatedResponseDto::class.java, Notificacion::class.java)
                    val pageAdapter = moshi.adapter<PaginatedResponseDto<Notificacion>>(pageType)
                    pageAdapter.fromJson(txt)?.data ?: emptyList()
                } else {
                    listAdapter.fromJson(txt) ?: emptyList()
                }

                Result.success(parsed)
            } else {
                Result.failure(IOException("Error al obtener las notificaciones: ${raw.code()}"))
            }
        } catch (e: Exception) {
            // Fallback: intentar la versión tipada (por si el RAW falla por alguna razón rara)
            try {
                val response = apiService.getNotificaciones()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(IOException("Error al obtener las notificaciones: ${response.code()}"))
                }
            } catch (ex: Exception) {
                Result.failure(ex)
            }
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

    override suspend fun setLeida(id: String): Result<Unit> {
        return try {
            val response = apiService.setLeida(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Error al marcar como leída: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun marcarTodasLeidas(): Result<Unit> {
        return try {
            val response = apiService.marcarTodasLeidas()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Error al marcar todas como leídas: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // PENDIENTE: Paging Notificaciones (dual-mode)
    // fun getNotificacionesPaged(perPage: Int): Flow<PagingData<Notificacion>> { ... }
}
