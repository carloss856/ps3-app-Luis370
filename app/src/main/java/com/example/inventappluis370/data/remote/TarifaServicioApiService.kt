package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.CreateTarifaRequest
import com.example.inventappluis370.data.model.TarifaServicio
import com.example.inventappluis370.data.model.UpdateTarifaRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para las operaciones del módulo de Tarifas de Servicio.
 */
interface TarifaServicioApiService {

    @GET("tarifas-servicio")
    suspend fun getTarifas(): Response<List<TarifaServicio>>

    @GET("tarifas-servicio")
    suspend fun getTarifasPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<TarifaServicio>>

    @POST("tarifas-servicio")
    suspend fun createTarifa(@Body request: CreateTarifaRequest): Response<Unit>

    @PUT("tarifas-servicio/{id}")
    suspend fun updateTarifa(@Path("id") id: String, @Body request: UpdateTarifaRequest): Response<Unit>

    @DELETE("tarifas-servicio/{id}")
    suspend fun deleteTarifa(@Path("id") id: String): Response<Unit>

    @GET("tarifas-servicio/{id}/historial")
    suspend fun getHistorialTarifa(@Path("id") id: String): Response<List<TarifaServicio>>
}
