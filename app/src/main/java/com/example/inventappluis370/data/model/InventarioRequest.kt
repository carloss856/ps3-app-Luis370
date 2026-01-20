package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Representa el cuerpo de la petición para crear una nueva entrada de inventario.
 */
data class InventarioRequest(
    @Json(name = "id_repuesto")
    val repuestoId: String,

    @Json(name = "cantidad_entrada")
    val cantidadEntrada: Int
)
