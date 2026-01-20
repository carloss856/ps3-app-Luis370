package com.example.inventappluis370.data.common

import com.example.inventappluis370.data.remote.ApiErrorParser
import retrofit2.Response

/**
 * Helper para envolver llamadas Retrofit en ApiResult.
 *
 * Mantiene el contrato:
 * - 401 => Unauthorized
 * - 403 => Forbidden
 * - 422 => Validation con fieldErrors
 */
suspend inline fun <reified T> safeApiCall(
    crossinline call: suspend () -> Response<T>
): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()

            // Muchos endpoints de este backend responden 200/201 sin body parseable.
            // Para esos casos (T = Unit), consideramos éxito aunque body sea null.
            if (body == null) {
                @Suppress("UNCHECKED_CAST")
                if (T::class == Unit::class) {
                    ApiResult.Success(Unit as T)
                } else {
                    ApiResult.Error.Http(response.code(), "Respuesta vacía")
                }
            } else {
                ApiResult.Success(body)
            }
        } else {
            when (response.code()) {
                401 -> ApiResult.Error.Unauthorized(ApiErrorParser.parseError(response))
                403 -> ApiResult.Error.Forbidden(
                    message = ApiErrorParser.parseError(response),
                    detailRaw = ApiErrorParser.parseDetailRaw(response)
                )
                422 -> {
                    val apiError = ApiErrorParser.parseErrorResponse(response)
                    ApiResult.Error.Validation(
                        message = apiError.message ?: "Validation error",
                        fieldErrors = apiError.errors ?: emptyMap()
                    )
                }
                else -> ApiResult.Error.Http(response.code(), ApiErrorParser.parseError(response))
            }
        }
    } catch (t: Throwable) {
        ApiResult.Error.Network(message = t.message ?: "Network error", throwable = t)
    }
}
