package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa el cuerpo de la petición para crear o actualizar un repuesto.
 */
@JsonClass(generateAdapter = true)
data class RepuestoRequest(
    @Json(name = "nombre_repuesto")
    val nombreRepuesto: String,

    @Json(name = "cantidad_disponible")
    val cantidadDisponible: Int?,

    @Json(name = "nivel_critico")
    val nivelCritico: Int?
)
