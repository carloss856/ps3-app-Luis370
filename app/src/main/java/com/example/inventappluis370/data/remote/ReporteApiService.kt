package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.Reporte
import com.example.inventappluis370.data.model.ReporteRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para las operaciones CRUD del módulo de Reportes.
 */
interface ReporteApiService {

    @GET("reportes")
    suspend fun getReportes(): Response<List<Reporte>>

    @GET("reportes")
    suspend fun getReportesPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<Reporte>>

    // Corregido: Usar ReporteRequest y no esperar cuerpo.
    @POST("reportes")
    suspend fun createReporte(@Body reporteRequest: ReporteRequest): Response<Unit>

    // Corregido: Usar ReporteRequest y no esperar cuerpo.
    @PUT("reportes/{id}")
    suspend fun updateReporte(@Path("id") id: String, @Body reporteRequest: ReporteRequest): Response<Unit>

    @DELETE("reportes/{id}")
    suspend fun deleteReporte(@Path("id") id: String): Response<Unit>
}
