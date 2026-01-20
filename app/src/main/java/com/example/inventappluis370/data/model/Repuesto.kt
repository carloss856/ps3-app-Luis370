package com.example.inventappluis370.data.model

import com.example.inventappluis370.core.network.LenientString
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad Repuesto según el contrato.
 *
 * Nota: id_repuesto debería venir siempre. Si falta, es un problema de datos/backend.
 */
@JsonClass(generateAdapter = true)
data class Repuesto(
    @field:Json(name = "id_repuesto")
    @field:LenientString
    val idRepuesto: String? = null,

    @field:Json(name = "nombre_repuesto")
    val nombreRepuesto: String? = null,

    @field:Json(name = "cantidad_disponible")
    val cantidadDisponible: Int? = null,

    @field:Json(name = "costo_unitario")
    val costoUnitario: Double? = null,

    @field:Json(name = "nivel_critico")
    val nivelCritico: Int? = null
)
