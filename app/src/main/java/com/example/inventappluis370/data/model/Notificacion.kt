package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad Notificación según el contrato.
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
    val estadoEnvio: String? = null,

    // 9.9: metadata para UI y lógica de leído/no leído
    val tipo: String? = null,

    @field:Json(name = "leida")
    val leida: Boolean? = null,

    @field:Json(name = "leida_en")
    val leidaEn: String? = null,
)
