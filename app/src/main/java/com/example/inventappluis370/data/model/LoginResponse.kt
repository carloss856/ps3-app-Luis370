package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la respuesta completa del endpoint de login.
 */
@JsonClass(generateAdapter = true)
data class LoginResponse(
    val usuario: Usuario? = null,
    val token: String? = null,
    val tipo: String? = null,
    @field:Json(name = "expires_at") val expiresAt: String? = null
)
