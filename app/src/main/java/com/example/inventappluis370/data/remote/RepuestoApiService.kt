package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.Repuesto
import com.example.inventappluis370.data.model.RepuestoRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para las operaciones CRUD del modulo de Repuestos.
 */
interface RepuestoApiService {

    /**
     * IMPORTANTE:
     * Algunos backends devuelven una lista directa (JSON array) y otros devuelven
     * un objeto paginado { data: [...], meta: {...} } aun sin parametros.
     *
     * Para evitar crashes de Moshi cuando cambia el formato, aqui obtenemos el body
     * como texto y lo parseamos en el repositorio con DualModePageParser.
     */
    @GET("repuestos")
    suspend fun getRepuestosRaw(): Response<ResponseBody>

    @GET("repuestos")
    suspend fun getRepuestosPaged(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.example.inventappluis370.data.model.PaginatedResponseDto<Repuesto>>

    @GET("repuestos/{id}")
    suspend fun getRepuestoById(@Path("id") id: String): Response<Repuesto>

    @POST("repuestos")
    suspend fun createRepuesto(@Body repuestoRequest: RepuestoRequest): Response<Unit>

    @PUT("repuestos/{id}")
    suspend fun updateRepuesto(@Path("id") id: String, @Body repuestoRequest: RepuestoRequest): Response<Unit>

    @DELETE("repuestos/{id}")
    suspend fun deleteRepuesto(@Path("id") id: String): Response<Unit>
}

