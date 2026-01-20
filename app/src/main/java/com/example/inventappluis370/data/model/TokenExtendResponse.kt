package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Respuesta del endpoint protegido POST /token/extend.
 *
 * Contrato confirmado:
 * - Body: { "message": "Token extended", "expires_at": "<ISO8601>" }
 * - Header: X-Token-Expires-At: <ISO8601>
 *
 * Nota: mantenemos compatibilidad hacia atrás por si el backend devuelve token/usuario/tipo.
 */
@JsonClass(generateAdapter = true)
data class TokenExtendResponse(
    val message: String? = null,
    val token: String? = null,
    val usuario: Usuario? = null,
    val tipo: String? = null,
    @field:Json(name = "expires_at") val expiresAt: String? = null
)
