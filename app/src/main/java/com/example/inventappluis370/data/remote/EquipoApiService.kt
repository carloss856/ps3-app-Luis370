package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.Equipo
import com.example.inventappluis370.data.model.EquipoRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para las operaciones CRUD del módulo de Equipos.
 */
interface EquipoApiService {

    @GET("equipos")
    suspend fun getEquipos(): Response<List<Equipo>>

    @GET("equipos")
    suspend fun getEquiposPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<Equipo>>

    // Corregido: No esperamos un cuerpo en la respuesta.
    @POST("equipos")
    suspend fun createEquipo(@Body equipoRequest: EquipoRequest): Response<Unit>

    // Corregido: No esperamos un cuerpo en la respuesta.
    @PUT("equipos/{id}")
    suspend fun updateEquipo(@Path("id") id: String, @Body equipoRequest: EquipoRequest): Response<Unit>

    @DELETE("equipos/{id}")
    suspend fun deleteEquipo(@Path("id") id: String): Response<Unit>
}
