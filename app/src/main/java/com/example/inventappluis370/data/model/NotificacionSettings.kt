package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la configuración de notificaciones de un usuario.
 */
@JsonClass(generateAdapter = true)
data class NotificacionSettings(
    @Json(name = "recibir_notificaciones")
    val recibirNotificaciones: Boolean? = null,

    @Json(name = "tipos_notificacion")
    val tiposNotificacion: List<String>? = null
)
