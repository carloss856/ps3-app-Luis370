package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Representa el cuerpo de la petición para crear o actualizar un repuesto.
 */
data class RepuestoRequest(
    @field:Json(name = "nombre_repuesto")
    val nombreRepuesto: String,

    @field:Json(name = "cantidad_disponible")
    val cantidadDisponible: Int?,

    @field:Json(name = "nivel_critico")
    val nivelCritico: Int?
)
