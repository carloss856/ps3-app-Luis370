package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.Inventario
import com.example.inventappluis370.data.model.InventarioRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para las operaciones CRUD del módulo de Inventario.
 */
interface InventarioApiService {

    @GET("inventario")
    suspend fun getInventario(): Response<List<Inventario>>

    @GET("inventario")
    suspend fun getInventarioPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<Inventario>>

    @GET("inventario/{id}")
    suspend fun getInventarioById(@Path("id") id: String): Response<Inventario>

    @POST("inventario")
    suspend fun createInventarioEntrada(@Body request: InventarioRequest): Response<Unit>

    @PUT("inventario/{id}")
    suspend fun updateInventarioEntrada(
        @Path("id") id: String, 
        @Body request: InventarioRequest
    ): Response<Unit>

    @DELETE("inventario/{id}")
    suspend fun deleteInventarioEntrada(@Path("id") id: String): Response<Unit>
}
