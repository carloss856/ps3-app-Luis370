package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Payload para crear o actualizar una garantía.
 */
data class GarantiaRequest(
    @field:Json(name = "id_servicio")
    val servicioId: String,

    @field:Json(name = "fecha_inicio")
    val fechaInicio: String,

    @field:Json(name = "fecha_fin")
    val fechaFin: String,

    val observaciones: String?,

    @field:Json(name = "validado_por_gerente")
    val validadoPorGerente: Boolean = false
)
