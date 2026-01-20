package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.MessageResponse
import com.example.inventappluis370.data.model.NotificacionSettings
import com.example.inventappluis370.data.model.UserRequest
import com.example.inventappluis370.data.model.Usuario
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para las operaciones CRUD del módulo de Usuarios.
 */
interface UsuarioApiService {

    @GET("usuarios")
    suspend fun getUsers(): Response<List<Usuario>>

    @GET("usuarios")
    suspend fun getUsersPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<Usuario>>

    /**
     * Variante RAW para soportar paginación dual-mode (array legacy vs {data,meta}).
     * Se usa desde DualModePagingSource.
     */
    @GET("usuarios")
    suspend fun getUsersPagedRaw(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<okhttp3.ResponseBody>

    // Corregido: No esperamos un cuerpo parseable.
    @POST("usuarios")
    suspend fun createUser(@Body userRequest: UserRequest): Response<Unit>

    // Corregido: No esperamos un cuerpo parseable.
    @PUT("usuarios/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body userRequest: UserRequest): Response<Unit>

    @DELETE("usuarios/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Unit>

    /**
     * Contrato: {id} recomendado = id_persona (ej: USR-XXXX).
     * El backend también puede soportar _id de Mongo, pero la app usa id_persona.
     */
    @GET("usuarios/{id}/notificaciones")
    suspend fun getNotificationSettings(@Path("id") userId: String): Response<NotificacionSettings>

    /**
     * Contrato: response 200 => {"message":"Configuración actualizada"}
     */
    @POST("usuarios/{id}/notificaciones")
    suspend fun saveNotificationSettings(
        @Path("id") userId: String,
        @Body settings: NotificacionSettings
    ): Response<MessageResponse>
}
