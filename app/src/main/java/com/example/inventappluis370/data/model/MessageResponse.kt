package com.example.inventappluis370.data.model

import com.squareup.moshi.JsonClass

/**
 * Respuesta típica de confirmación:
 * { "message": "..." }
 */
@JsonClass(generateAdapter = true)
data class MessageResponse(
    val message: String? = null,
)

