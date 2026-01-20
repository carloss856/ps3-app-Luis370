package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Payload para crear o actualizar un servicio.
 */
data class CreateServicioRequest(
    @field:Json(name = "id_equipo")
    val idEquipo: String,

    @field:Json(name = "codigo_rma")
    val codigoRma: String,

    @field:Json(name = "fecha_ingreso")
    val fechaIngreso: String,

    @field:Json(name = "problema_reportado")
    val problemaReportado: String,

    val estado: String,

    @field:Json(name = "costo_estimado")
    val costoEstimado: Double? = null,

    @field:Json(name = "costo_real")
    val costoReal: Double? = null,

    @field:Json(name = "validado_por_gerente")
    val validadoPorGerente: Boolean? = null
)
