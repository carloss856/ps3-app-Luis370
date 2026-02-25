package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Payload para crear una nueva solicitud de repuesto.
 */
@JsonClass(generateAdapter = true)
data class CreateSolicitudRequest(
    @Json(name = "id_repuesto")
    val repuestoId: String,

    @Json(name = "id_servicio")
    val servicioId: String,

    @Json(name = "cantidad_solicitada")
    val cantidadSolicitada: Int,

    @Json(name = "id_usuario")
    val idUsuario: String,

    @Json(name = "estado_solicitud")
    val estadoSolicitud: String? = "Pendiente",

    val comentarios: String?
)

