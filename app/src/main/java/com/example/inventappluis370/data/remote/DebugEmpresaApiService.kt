package com.example.inventappluis370.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Endpoint RAW para depurar errores de validacion (422) sin depender del mapeo de DTOs.
 *
 * Ojo: esto NO se usa en produccion; es para confirmar que JSON esta recibiendo el backend.
 */
interface DebugEmpresaApiService {

    @POST("empresas")
    suspend fun createEmpresaRaw(@Body body: RequestBody): Response<ResponseBody>

    @PUT("empresas/{id}")
    suspend fun updateEmpresaRaw(
        @Path("id") id: String,
        @Body body: RequestBody
    ): Response<ResponseBody>
}

