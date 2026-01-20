package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad SolicitudRepuesto según el contrato.
 *
 * Nota: id_solicitud debería venir siempre. Si falta, es un problema de datos/backend.
 */
@JsonClass(generateAdapter = true)
data class SolicitudRepuesto(
    @field:Json(name = "id_solicitud")
    val idSolicitud: String? = null,

    @field:Json(name = "id_repuesto")
    val idRepuesto: String? = null,

    @field:Json(name = "id_servicio")
    val idServicio: String? = null,

    @field:Json(name = "cantidad_solicitada")
    val cantidadSolicitada: Int? = null,

    @field:Json(name = "id_usuario")
    val idUsuario: String? = null,

    @field:Json(name = "fecha_solicitud")
    val fechaSolicitud: String? = null,

    @field:Json(name = "estado_solicitud")
    val estadoSolicitud: String? = null,

    val comentarios: String? = null
)
