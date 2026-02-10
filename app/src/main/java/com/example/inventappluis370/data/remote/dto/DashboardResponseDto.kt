package com.example.inventappluis370.data.remote.dto

import com.example.inventappluis370.core.network.LenientString
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DashboardResponseDto(
    @Json(name = "role") val role: String? = null,
    @Json(name = "cards") val cards: List<KpiCardDto> = emptyList(),
    @Json(name = "lists") val lists: DashboardListsDto? = null,
)

@JsonClass(generateAdapter = true)
data class KpiCardDto(
    @Json(name = "key") val key: String = "",
    @Json(name = "title") val title: String = "",
    /**
     * El backend debería mandar número/int, pero en legacy puede venir como number/string/boolean.
     * Lo tipamos a String con adapter leniente para evitar que Moshi caiga en ObjectJsonAdapter.
     */
    @LenientString
    @Json(name = "value") val value: String? = null,
)

@JsonClass(generateAdapter = true)
data class DashboardListsDto(
    @Json(name = "repuestos_criticos") val repuestosCriticos: List<RepuestoCriticoDto>? = null,
    @Json(name = "notificaciones_recientes") val notificacionesRecientes: List<NotificacionRecienteDto>? = null,
)

@JsonClass(generateAdapter = true)
data class RepuestoCriticoDto(
    @Json(name = "id_repuesto") val idRepuesto: String? = null,
    @Json(name = "nombre_repuesto") val nombreRepuesto: String? = null,
    @Json(name = "cantidad_disponible") val cantidadDisponible: Int? = null,
    @Json(name = "nivel_critico") val nivelCritico: Int? = null,
)

@JsonClass(generateAdapter = true)
data class NotificacionRecienteDto(
    @Json(name = "id_notificacion") val idNotificacion: String? = null,
    @Json(name = "asunto") val asunto: String? = null,
    @Json(name = "estado_envio") val estadoEnvio: String? = null,
    @Json(name = "fecha_envio") val fechaEnvio: String? = null,
)
