package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad Inventario segun el contrato.
 *
 * Nota: id_entrada deberia venir siempre. Si falta, es un problema de datos/backend.
 */
@JsonClass(generateAdapter = true)
data class Inventario(
    @field:Json(name = "id_entrada")
    val idEntrada: String? = null,

    @field:Json(name = "id_repuesto")
    val idRepuesto: String? = null,

    @field:Json(name = "cantidad_entrada")
    val cantidadEntrada: Int? = null,

    @field:Json(name = "fecha_entrada")
    val fechaEntrada: String? = null
)

