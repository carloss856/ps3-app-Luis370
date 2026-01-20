package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.Empresa
import com.example.inventappluis370.data.model.EmpresaRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para las operaciones CRUD del módulo de Empresas.
 */
interface EmpresaApiService {

    @GET("empresas")
    suspend fun getEmpresas(): Response<List<Empresa>>

    @GET("empresas")
    suspend fun getEmpresasPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<Empresa>>

    @GET("empresas/{id}")
    suspend fun getEmpresaById(@Path("id") id: String): Response<Empresa>

    // Corregido: No esperamos un cuerpo en la respuesta, solo el código de éxito.
    @POST("empresas")
    suspend fun createEmpresa(@Body empresaRequest: EmpresaRequest): Response<Unit>

    // Corregido: No esperamos un cuerpo en la respuesta, solo el código de éxito.
    @PUT("empresas/{id}")
    suspend fun updateEmpresa(@Path("id") id: String, @Body empresaRequest: EmpresaRequest): Response<Unit>

    @DELETE("empresas/{id}")
    suspend fun deleteEmpresa(@Path("id") id: String): Response<Unit>
}
