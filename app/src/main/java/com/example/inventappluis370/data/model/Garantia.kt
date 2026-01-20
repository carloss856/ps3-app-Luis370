package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad Garantía según el contrato.
 *
 * Nota: id_garantia debería venir siempre. Si falta, es un problema de datos/backend.
 */
@JsonClass(generateAdapter = true)
data class Garantia(
    @field:Json(name = "id_garantia")
    val idGarantia: String? = null,

    @field:Json(name = "id_servicio")
    val servicioId: String? = null,

    @field:Json(name = "fecha_inicio")
    val fechaInicio: String? = null,

    @field:Json(name = "fecha_fin")
    val fechaFin: String? = null,

    val observaciones: String? = null,

    @field:Json(name = "validado_por_gerente")
    val validadoPorGerente: Boolean? = null
)
