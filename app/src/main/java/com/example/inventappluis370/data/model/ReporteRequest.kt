package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Representa el cuerpo de la petición para crear un nuevo reporte.
 */
data class ReporteRequest(
    @Json(name = "tipo_reporte")
    val tipoReporte: String,

    @Json(name = "parametros_utilizados")
    val parametrosUtilizados: String,

    @Json(name = "id_usuario")
    val idUsuario: String
)
