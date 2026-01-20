package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.RMA
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de Retrofit para las operaciones del módulo de RMA.
 */
interface RmaApiService {

    @GET("rma")
    suspend fun getRmas(): Response<List<RMA>>

    @GET("rma")
    suspend fun getRmasPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<RMA>>

    @DELETE("rma/{id}")
    suspend fun deleteRma(@Path("id") id: String): Response<Unit>
}
