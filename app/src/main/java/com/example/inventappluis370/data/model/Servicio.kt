package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad Servicio según el contrato.
 *
 * Nota: id_servicio debería venir siempre. Si falta, es un problema de datos/backend.
 * Se tolera null para evitar crash de Moshi y poder diagnosticar desde UI.
 */
@JsonClass(generateAdapter = true)
data class Servicio(
    @field:Json(name = "id_servicio")
    val idServicio: String? = null,

    @field:Json(name = "id_equipo")
    val idEquipo: String? = null,

    @field:Json(name = "codigo_rma")
    val codigoRma: String? = null,

    @field:Json(name = "fecha_ingreso")
    val fechaIngreso: String? = null,

    @field:Json(name = "problema_reportado")
    val problemaReportado: String? = null,

    val estado: String? = null,

    @field:Json(name = "costo_estimado")
    val costoEstimado: Double? = null,

    @field:Json(name = "costo_real")
    val costoReal: Double? = null,

    @field:Json(name = "validado_por_gerente")
    val validadoPorGerente: Boolean? = null,

    @field:Json(name = "partes_trabajo")
    val partesTrabajo: List<ParteTrabajo>? = null,

    @field:Json(name = "costo_mano_obra")
    val costoManoObra: Double? = null,

    @field:Json(name = "tiempo_total_minutos")
    val tiempoTotalMinutos: Int? = null
)
