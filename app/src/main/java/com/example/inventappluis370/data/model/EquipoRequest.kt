package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Payload para crear o actualizar un equipo.
 */
data class EquipoRequest(
    @field:Json(name = "tipo_equipo")
    val tipoEquipo: String,

    val marca: String?,
    val modelo: String?,

    @field:Json(name = "id_persona")
    val idPersona: String?,

    @field:Json(name = "id_asignado")
    val idAsignado: String?
)
