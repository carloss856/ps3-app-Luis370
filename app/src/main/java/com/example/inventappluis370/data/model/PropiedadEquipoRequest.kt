package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Payload para crear o actualizar la propiedad de un equipo.
 */
data class PropiedadEquipoRequest(
    @field:Json(name = "id_equipo")
    val idEquipo: String,
    
    @field:Json(name = "id_persona")
    val idPersona: String
)
