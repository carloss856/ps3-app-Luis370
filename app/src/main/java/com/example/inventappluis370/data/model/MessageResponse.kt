package com.example.inventappluis370.data.model

import com.squareup.moshi.JsonClass

/**
 * Respuesta tipica de confirmacion:
 * { "message": "..." }
 */
@JsonClass(generateAdapter = true)
data class MessageResponse(
    val message: String? = null,
)


