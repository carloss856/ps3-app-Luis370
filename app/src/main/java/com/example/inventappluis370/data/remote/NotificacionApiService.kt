package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.Notificacion
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de Retrofit para las operaciones del módulo de Notificaciones.
 */
interface NotificacionApiService {

    /**
     * Variante RAW para tolerar respuestas vacías o cambios de formato (array vs wrapper).
     * Se parsea manualmente en el repositorio.
     */
    @GET("notificaciones")
    suspend fun getNotificacionesRaw(): Response<ResponseBody>

    @GET("notificaciones")
    suspend fun getNotificaciones(): Response<List<Notificacion>>

    @GET("notificaciones")
    suspend fun getNotificacionesPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<Notificacion>>

    @DELETE("notificaciones/{id}")
    suspend fun deleteNotificacion(@Path("id") id: String): Response<Unit>

    @PATCH("notificaciones/{id}/leida")
    suspend fun setLeida(@Path("id") id: String): Response<Unit>

    @POST("notificaciones/marcar-todas-leidas")
    suspend fun marcarTodasLeidas(): Response<Unit>
}
