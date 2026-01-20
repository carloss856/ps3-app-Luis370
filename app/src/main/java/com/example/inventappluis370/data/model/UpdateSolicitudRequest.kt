package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Payload para actualizar una solicitud de repuesto.
 */
data class UpdateSolicitudRequest(
    @field:Json(name = "id_repuesto")
    val repuestoId: String,

    @field:Json(name = "id_servicio")
    val servicioId: String,

    @field:Json(name = "cantidad_solicitada")
    val cantidadSolicitada: Int,

    @field:Json(name = "id_usuario")
    val idUsuario: String,

    @field:Json(name = "estado_solicitud")
    val estadoSolicitud: String,

    val comentarios: String?
)
