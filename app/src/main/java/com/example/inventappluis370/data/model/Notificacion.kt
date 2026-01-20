package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad Notificación según el contrato.
 *
 * Nota: id_notificacion debería venir siempre. Si falta, es un problema de datos/backend.
 */
@JsonClass(generateAdapter = true)
data class Notificacion(
    @field:Json(name = "id_notificacion")
    val idNotificacion: String? = null,

    @field:Json(name = "id_servicio")
    val idServicio: String? = null,

    @field:Json(name = "email_destinatario")
    val emailDestinatario: String? = null,

    val asunto: String? = null,
    val mensaje: String? = null,

    @field:Json(name = "fecha_envio")
    val fechaEnvio: String? = null,

    @field:Json(name = "estado_envio")
    val estadoEnvio: String? = null
)
