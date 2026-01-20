package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad Reporte según el contrato.
 *
 * Nota: id_reporte debería venir siempre. Si falta, es un problema de datos/backend.
 */
@JsonClass(generateAdapter = true)
data class Reporte(
    @field:Json(name = "id_reporte")
    val idReporte: String? = null,

    @field:Json(name = "tipo_reporte")
    val tipoReporte: String? = null,

    @field:Json(name = "fecha_generacion")
    val fechaGeneracion: String? = null,

    @field:Json(name = "parametros_utilizados")
    val parametrosUtilizados: String? = null,

    @field:Json(name = "id_usuario")
    val idUsuario: String? = null
)
