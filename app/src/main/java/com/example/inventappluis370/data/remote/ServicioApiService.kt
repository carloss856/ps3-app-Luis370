package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.*
import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para las operaciones del módulo de Servicios y sus Partes de Trabajo.
 */
interface ServicioApiService {

    @GET("servicios")
    suspend fun getServicios(): Response<List<Servicio>>

    @GET("servicios")
    suspend fun getServiciosPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<Servicio>>

    /**
     * Variante RAW para tolerar respuestas legacy (array) o wrappers no estándar.
     * Se parsea con DualModePageParser en el PagingSource.
     */
    @GET("servicios")
    suspend fun getServiciosRaw(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<okhttp3.ResponseBody>

    @GET("servicios/{id}")
    suspend fun getServicioById(@Path("id") id: String): Response<Servicio>

    @POST("servicios")
    suspend fun createServicio(@Body request: CreateServicioRequest): Response<Unit>

    @PUT("servicios/{id}")
    suspend fun updateServicio(
        @Path("id") id: String, 
        @Body request: CreateServicioRequest
    ): Response<Unit>

    @DELETE("servicios/{id}")
    suspend fun deleteServicio(@Path("id") id: String): Response<Unit>

    // --- Partes de Trabajo ---

    @GET("servicios/{id}/partes")
    suspend fun getPartes(@Path("id") servicioId: String): Response<PartesResponse>

    @POST("servicios/{id}/partes")
    suspend fun createParte(
        @Path("id") servicioId: String,
        @Body request: CreateParteTrabajoRequest
    ): Response<Unit>

    @PUT("servicios/{id_servicio}/partes/{id_parte}")
    suspend fun updateParte(
        @Path("id_servicio") servicioId: String,
        @Path("id_parte") parteId: String,
        @Body request: UpdateParteTrabajoRequest
    ): Response<Unit>

    @DELETE("servicios/{id_servicio}/partes/{id_parte}")
    suspend fun deleteParte(
        @Path("id_servicio") servicioId: String,
        @Path("id_parte") parteId: String
    ): Response<Unit>
}

/**
 * Modelo de respuesta para el listado de partes de trabajo.
 */
data class PartesResponse(
    @field:Json(name = "id_servicio") val servicioId: String,
    @field:Json(name = "partes_trabajo") val partes: List<ParteTrabajo>,
    @field:Json(name = "costo_mano_obra") val costoManoObra: Double,
    @field:Json(name = "tiempo_total_minutos") val tiempoTotalMinutos: Int
)
