package com.example.inventappluis370.data.common

/**
 * Resultado estándar de llamadas de red/repo.
 *
 * - No inventa formatos: está pensado para mapear
 *   401/403/{message} y 422 {message, errors:{campo:[...]}} del contrato.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()

    sealed class Error : ApiResult<Nothing>() {
        data class Unauthorized(val message: String = "Unauthorized") : Error()

        /**
         * Contrato: 403 puede venir como {"message":"Forbidden","detail":{...}}.
         * Guardamos detail como rawJson para mostrarlo o loguearlo sin inventar estructura.
         */
        data class Forbidden(
            val message: String = "Forbidden",
            val detailRaw: String? = null
        ) : Error()

        data class Validation(
            val message: String = "Validation error",
            val fieldErrors: Map<String, List<String>> = emptyMap()
        ) : Error()

        data class Http(val code: Int, val message: String) : Error()
        data class Network(val message: String, val throwable: Throwable? = null) : Error()
        data class Unknown(val message: String, val throwable: Throwable? = null) : Error()
    }
}
