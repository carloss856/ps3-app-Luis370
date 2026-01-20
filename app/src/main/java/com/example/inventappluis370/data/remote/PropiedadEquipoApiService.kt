package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.PropiedadEquipo
import com.example.inventappluis370.data.model.PropiedadEquipoRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para las operaciones CRUD del módulo de Propiedad de Equipos.
 */
interface PropiedadEquipoApiService {

    @GET("propiedad-equipos")
    suspend fun getPropiedades(): Response<List<PropiedadEquipo>>

    @GET("propiedad-equipos")
    suspend fun getPropiedadesPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<PropiedadEquipo>>

    @GET("propiedad-equipo/{id_equipo}")
    suspend fun getByEquipo(@Path("id_equipo") equipoId: String): Response<PropiedadEquipo>

    @POST("propiedad-equipos")
    suspend fun createPropiedad(@Body request: PropiedadEquipoRequest): Response<Unit>

    @PUT("propiedad-equipos/{id}")
    suspend fun updatePropiedad(
        @Path("id") id: String,
        @Body request: PropiedadEquipoRequest
    ): Response<Unit>

    @DELETE("propiedad-equipos/{id}")
    suspend fun deletePropiedad(@Path("id") id: String): Response<Unit>
}
