package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.Garantia
import com.example.inventappluis370.data.model.GarantiaRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para las operaciones del módulo de Garantías.
 */
interface GarantiaApiService {

    @GET("garantias")
    suspend fun getGarantias(): Response<List<Garantia>>

    @GET("garantias")
    suspend fun getGarantiasPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<Garantia>>

    @GET("garantias/{id}")
    suspend fun getGarantiaById(@Path("id") id: String): Response<Garantia>

    // Corregido: Usar GarantiaRequest y no esperar cuerpo.
    @POST("garantias")
    suspend fun createGarantia(@Body request: GarantiaRequest): Response<Unit>

    // Corregido: Usar GarantiaRequest y no esperar cuerpo.
    @PUT("garantias/{id}")
    suspend fun updateGarantia(@Path("id") id: String, @Body request: GarantiaRequest): Response<Unit>

    @DELETE("garantias/{id}")
    suspend fun deleteGarantia(@Path("id") id: String): Response<Unit>
}
