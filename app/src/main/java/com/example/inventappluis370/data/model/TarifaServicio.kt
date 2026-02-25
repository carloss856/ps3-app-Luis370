package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa una tarifa de servicio segun el contrato.
 *
 * Nota: id_tarifa deberia venir siempre. Si falta, es un problema de datos/backend.
 */
@JsonClass(generateAdapter = true)
data class TarifaServicio(
    @field:Json(name = "id_tarifa")
    val idTarifa: String? = null,

    @field:Json(name = "tipo_tarea")
    val tipoTarea: String? = null,

    @field:Json(name = "nivel_tecnico")
    val nivelTecnico: String? = null,

    @field:Json(name = "tarifa_hora")
    val tarifaHora: Double? = null,

    val moneda: String? = null,
    val activo: Boolean? = null,

    @field:Json(name = "vigente_desde")
    val vigenteDesde: String? = null,

    @field:Json(name = "vigente_hasta")
    val vigenteHasta: String? = null
)

/**
 * Payload para crear una tarifa de servicio.
 */
data class CreateTarifaRequest(
    @field:Json(name = "tipo_tarea")
    val tipoTarea: String,

    @field:Json(name = "nivel_tecnico")
    val nivelTecnico: String?,

    @field:Json(name = "tarifa_hora")
    val tarifaHora: Double,

    val moneda: String,
    val activo: Boolean,

    @field:Json(name = "vigente_desde")
    val vigenteDesde: String
)

/**
 * Payload para editar una tarifa de servicio.
 */
data class UpdateTarifaRequest(
    @field:Json(name = "tarifa_hora")
    val tarifaHora: Double,
    val moneda: String,
    val activo: Boolean
)

/**
 * Representa el historial de cambios de una tarifa de servicio.
 */
@JsonClass(generateAdapter = true)
data class TarifaServicioHistorial(
    @field:Json(name = "id_historial")
    val idHistorial: String? = null,

    @field:Json(name = "id_tarifa")
    val idTarifa: String? = null,

    @field:Json(name = "tipo_tarea")
    val tipoTarea: String? = null,

    @field:Json(name = "nivel_tecnico")
    val nivelTecnico: String? = null,

    @field:Json(name = "tarifa_hora")
    val tarifaHora: Double? = null,

    val moneda: String? = null,

    @field:Json(name = "fecha_registro")
    val fechaRegistro: String? = null,

    @field:Json(name = "id_usuario")
    val idUsuario: String? = null,

    @field:Json(name = "nombre_usuario")
    val nombreUsuario: String? = null,
)

