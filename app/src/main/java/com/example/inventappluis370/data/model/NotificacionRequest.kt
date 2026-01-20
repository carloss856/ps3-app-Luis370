package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Payload para crear o actualizar una notificación.
 */
data class NotificacionRequest(
    @field:Json(name = "id_servicio")
    val idServicio: String,

    @field:Json(name = "email_destinatario")
    val emailDestinatario: String,

    val asunto: String,
    val mensaje: String,

    @field:Json(name = "estado_envio")
    val estadoEnvio: String = "Pendiente"
)
