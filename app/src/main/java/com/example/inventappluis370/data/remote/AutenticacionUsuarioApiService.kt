package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.AutenticacionUsuario
import com.example.inventappluis370.data.model.PaginatedResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * GET /autenticacion-usuarios
 * Contrato: soporta paginación dual-mode.
 */
interface AutenticacionUsuarioApiService {

    @GET("autenticacion-usuarios")
    suspend fun getAutenticaciones(): Response<List<AutenticacionUsuario>>

    @GET("autenticacion-usuarios")
    suspend fun getAutenticacionesPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<PaginatedResponseDto<AutenticacionUsuario>>
}

