package com.example.inventappluis370.data.common

import com.squareup.moshi.Json

/**
 * Modelo general de error del backend según contrato:
 * - 401/403: {"message":"..."}
 * - 422: {"message":"...", "errors": { "campo": ["..."] } }
 */
data class ApiErrorResponse(
    val message: String? = null,
    val error: String? = null,
    val errors: Map<String, List<String>>? = null
)

/**
 * Excepción de validación para propagar errores por campo hasta UI.
 */
class ValidationException(
    val fieldErrors: Map<String, List<String>>,
    override val message: String
) : RuntimeException(message)

