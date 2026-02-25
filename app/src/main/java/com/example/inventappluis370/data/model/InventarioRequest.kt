package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa el cuerpo de la peticion para crear una nueva entrada de inventario.
 */
@JsonClass(generateAdapter = true)
data class InventarioRequest(
    @Json(name = "id_repuesto")
    val repuestoId: String,

    @Json(name = "cantidad_entrada")
    val cantidadEntrada: Int
)

