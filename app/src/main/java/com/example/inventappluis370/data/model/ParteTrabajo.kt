package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Parte de trabajo embebida en Servicio.partes_trabajo.
 * La estructura completa puede variar; toleramos nulls para evitar crashes.
 */
@JsonClass(generateAdapter = true)
data class ParteTrabajo(
    @field:Json(name = "id_parte") val idParte: String? = null,
    @field:Json(name = "id_tecnico") val idTecnico: String? = null,
    @field:Json(name = "tipo_tarea") val tipoTarea: String? = null,
    val minutos: Int? = null,
    val notas: String? = null,
    @field:Json(name = "tarifa_hora") val tarifaHora: Double? = null,
    val moneda: String? = null,
    val costo: Double? = null
)
