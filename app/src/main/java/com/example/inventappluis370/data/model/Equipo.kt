package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad Equipo según el contrato.
 *
 * Nota: id_equipo debería venir siempre. Si falta, es un problema de datos/backend.
 */
@JsonClass(generateAdapter = true)
data class Equipo(
    @field:Json(name = "id_equipo")
    val idEquipo: String? = null,

    @field:Json(name = "tipo_equipo")
    val tipoEquipo: String? = null,

    val marca: String? = null,
    val modelo: String? = null,

    @field:Json(name = "id_asignado")
    val idAsignado: String? = null,

    val propiedad: PropiedadEquipo? = null
)
