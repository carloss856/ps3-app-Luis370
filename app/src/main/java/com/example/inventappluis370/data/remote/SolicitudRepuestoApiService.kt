package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.CreateSolicitudRequest
import com.example.inventappluis370.data.model.SolicitudRepuesto
import com.example.inventappluis370.data.model.UpdateSolicitudRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para las operaciones del módulo de Solicitudes de Repuestos.
 */
interface SolicitudRepuestoApiService {

    @GET("solicitud-repuestos")
    suspend fun getSolicitudes(): Response<List<SolicitudRepuesto>>

    @GET("solicitud-repuestos")
    suspend fun getSolicitudesPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<SolicitudRepuesto>>

    /**
     * Variante RAW para diagnóstico: evita fallos de parseo Moshi cuando el backend responde con
     * wrappers/errores no estándar.
     */
    @GET("solicitud-repuestos")
    suspend fun getSolicitudesRaw(): Response<ResponseBody>

    @GET("solicitud-repuestos/{id}")
    suspend fun getSolicitudById(@Path("id") id: String): Response<SolicitudRepuesto>

    @POST("solicitud-repuestos")
    suspend fun createSolicitud(@Body request: CreateSolicitudRequest): Response<Unit>

    @PUT("solicitud-repuestos/{id}")
    suspend fun updateSolicitud(
        @Path("id") id: String,
        @Body request: UpdateSolicitudRequest
    ): Response<Unit>

    @DELETE("solicitud-repuestos/{id}")
    suspend fun deleteSolicitud(@Path("id") id: String): Response<Unit>
}
