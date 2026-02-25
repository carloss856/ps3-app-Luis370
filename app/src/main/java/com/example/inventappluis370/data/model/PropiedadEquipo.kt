package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad PropiedadEquipo segun el contrato.
 *
 * Nota: id_propiedad deberia venir siempre. Si falta, es un problema de datos/backend.
 */
@JsonClass(generateAdapter = true)
data class PropiedadEquipo(
    @field:Json(name = "id_propiedad")
    val idPropiedad: String? = null,

    @field:Json(name = "id_equipo")
    val idEquipo: String? = null,

    @field:Json(name = "id_persona")
    val idPersona: String? = null
)

