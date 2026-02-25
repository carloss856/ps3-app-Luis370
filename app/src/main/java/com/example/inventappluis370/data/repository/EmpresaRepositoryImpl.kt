package com.example.inventappluis370.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.inventappluis370.BuildConfig
import com.example.inventappluis370.data.common.ValidationException
import com.example.inventappluis370.data.model.Empresa
import com.example.inventappluis370.data.model.EmpresaRequest
import com.example.inventappluis370.data.paging.GenericPagingSource
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.EmpresaApiService
import com.example.inventappluis370.domain.repository.EmpresaRepository
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Singleton
class EmpresaRepositoryImpl @Inject constructor(
    private val apiService: EmpresaApiService,
    private val debugApiService: com.example.inventappluis370.data.remote.DebugEmpresaApiService
) : EmpresaRepository {

    override suspend fun getEmpresas(): Result<List<Empresa>> {
        return try {
            val response = apiService.getEmpresas()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEmpresaById(id: String): Result<Empresa> {
        return try {
            val response = apiService.getEmpresaById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createEmpresa(empresaRequest: EmpresaRequest): Result<Unit> {
        return try {
            val trimmedNombre = empresaRequest.nombreEmpresa.trim()
            val trimmedEmail = empresaRequest.email.trim()

            if (BuildConfig.DEBUG) {
                Log.d(
                    "EmpresaRepository",
                    "POST /empresas payload nombre_empresa='${trimmedNombre}' email='${trimmedEmail}' direccion='${empresaRequest.direccion}' telefono='${empresaRequest.telefono}'"
                )
            }

            // Aseguramos que nunca se envien strings con espacios que el backend interprete como vacio.
            val safeRequest = empresaRequest.copy(
                nombreEmpresa = trimmedNombre,
                email = trimmedEmail,
                direccion = empresaRequest.direccion?.trim()?.ifBlank { null },
                telefono = empresaRequest.telefono?.trim()?.ifBlank { null }
            )

            val response = apiService.createEmpresa(safeRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val parsed = ApiErrorParser.parseErrorResponse(response)
                if (BuildConfig.DEBUG) {
                    Log.d(
                        "EmpresaRepository",
                        "POST /empresas failed status=${response.code()} message='${parsed.message}' errors=${parsed.errors}"
                    )
                }

                // Debug RAW: imprime el JSON exacto en snake_case que Android cree estar enviando.
                if (BuildConfig.DEBUG && (response.code() == 422 || parsed.message?.contains("validation", true) == true)) {
                    runCatching {
                        val json = buildString {
                            append('{')
                            append("\"nombre_empresa\":")
                            append(JSONObject.quote(safeRequest.nombreEmpresa))
                            append(',')
                            append("\"email\":")
                            append(JSONObject.quote(safeRequest.email))
                            if (!safeRequest.direccion.isNullOrBlank()) {
                                append(',')
                                append("\"direccion\":")
                                append(JSONObject.quote(safeRequest.direccion))
                            }
                            if (!safeRequest.telefono.isNullOrBlank()) {
                                append(',')
                                append("\"telefono\":")
                                append(JSONObject.quote(safeRequest.telefono))
                            }
                            if (!safeRequest.fechaCreacion.isNullOrBlank()) {
                                append(',')
                                append("\"fecha_creacion\":")
                                append(JSONObject.quote(safeRequest.fechaCreacion))
                            }
                            append('}')
                        }
                        Log.d("EmpresaRepository", "DEBUG raw POST /empresas sending=$json")

                        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
                        val rawResp = debugApiService.createEmpresaRaw(body)
                        val rawText = rawResp.errorBody()?.string() ?: rawResp.body()?.string()
                        Log.d(
                            "EmpresaRepository",
                            "DEBUG raw POST /empresas status=${rawResp.code()} body=${rawText ?: "<empty>"}"
                        )
                    }.onFailure { t ->
                        Log.w("EmpresaRepository", "DEBUG raw POST /empresas fallo", t)
                    }
                }

                if (response.code() == 422 && !parsed.errors.isNullOrEmpty()) {
                    Result.failure(
                        ValidationException(
                            fieldErrors = parsed.errors,
                            message = parsed.message ?: "Error de validacion"
                        )
                    )
                } else {
                    // Si el backend no manda dictionary de errors, al menos devuelve el message.
                    Result.failure(IOException(parsed.message ?: ApiErrorParser.parseError(response)))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmpresa(id: String, empresaRequest: EmpresaRequest): Result<Unit> {
        return try {
            val trimmedNombre = empresaRequest.nombreEmpresa.trim()
            val trimmedEmail = empresaRequest.email.trim()

            if (BuildConfig.DEBUG) {
                Log.d(
                    "EmpresaRepository",
                    "PUT /empresas/$id payload nombre_empresa='${trimmedNombre}' email='${trimmedEmail}' direccion='${empresaRequest.direccion}' telefono='${empresaRequest.telefono}'"
                )
            }

            val safeRequest = empresaRequest.copy(
                nombreEmpresa = trimmedNombre,
                email = trimmedEmail,
                direccion = empresaRequest.direccion?.trim()?.ifBlank { null },
                telefono = empresaRequest.telefono?.trim()?.ifBlank { null }
            )

            val response = apiService.updateEmpresa(id, safeRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val parsed = ApiErrorParser.parseErrorResponse(response)
                if (BuildConfig.DEBUG) {
                    Log.d(
                        "EmpresaRepository",
                        "PUT /empresas/$id failed status=${response.code()} message='${parsed.message}' errors=${parsed.errors}"
                    )
                }

                if (BuildConfig.DEBUG && (response.code() == 422 || parsed.message?.contains("validation", true) == true)) {
                    runCatching {
                        val json = buildString {
                            append('{')
                            append("\"nombre_empresa\":")
                            append(JSONObject.quote(safeRequest.nombreEmpresa))
                            append(',')
                            append("\"email\":")
                            append(JSONObject.quote(safeRequest.email))
                            if (!safeRequest.direccion.isNullOrBlank()) {
                                append(',')
                                append("\"direccion\":")
                                append(JSONObject.quote(safeRequest.direccion))
                            }
                            if (!safeRequest.telefono.isNullOrBlank()) {
                                append(',')
                                append("\"telefono\":")
                                append(JSONObject.quote(safeRequest.telefono))
                            }
                            append('}')
                        }
                        Log.d("EmpresaRepository", "DEBUG raw PUT /empresas/$id sending=$json")

                        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
                        val rawResp = debugApiService.updateEmpresaRaw(id, body)
                        val rawText = rawResp.errorBody()?.string() ?: rawResp.body()?.string()
                        Log.d(
                            "EmpresaRepository",
                            "DEBUG raw PUT /empresas/$id status=${rawResp.code()} body=${rawText ?: "<empty>"}"
                        )
                    }.onFailure { t ->
                        Log.w("EmpresaRepository", "DEBUG raw PUT /empresas/$id fallo", t)
                    }
                }

                if (response.code() == 422 && !parsed.errors.isNullOrEmpty()) {
                    Result.failure(
                        ValidationException(
                            fieldErrors = parsed.errors,
                            message = parsed.message ?: "Error de validacion"
                        )
                    )
                } else {
                    Result.failure(IOException(parsed.message ?: ApiErrorParser.parseError(response)))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEmpresa(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteEmpresa(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getEmpresasPaged(perPage: Int): Flow<PagingData<Empresa>> {
        return Pager(
            config = PagingConfig(
                pageSize = perPage,
                enablePlaceholders = false,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                GenericPagingSource(
                    perPage = perPage,
                    fetch = { page, pp -> apiService.getEmpresasPaged(page = page, perPage = pp) }
                )
            }
        ).flow
    }
}

