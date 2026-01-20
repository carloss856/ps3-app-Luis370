package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad RMA según el contrato.
 *
 * Nota: rma debería venir siempre. Si falta, es un problema de datos/backend.
 */
@JsonClass(generateAdapter = true)
data class RMA(
    val rma: String? = null,

    @field:Json(name = "id_persona")
    val idPersona: String? = null,

    @field:Json(name = "fecha_creacion")
    val fechaCreacion: String? = null
)
