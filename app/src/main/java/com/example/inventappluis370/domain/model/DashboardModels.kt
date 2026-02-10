package com.example.inventappluis370.domain.model

/** Modelos de dominio para la pantalla "Estadísticas" (Dashboard real). */

data class DashboardKpiCard(
    val key: String,
    val title: String,
    val value: Any?,
)

data class DashboardLists(
    val repuestosCriticos: List<RepuestoCritico> = emptyList(),
    val notificacionesRecientes: List<NotificacionReciente> = emptyList(),
)

data class RepuestoCritico(
    val idRepuesto: String,
    val nombreRepuesto: String,
    val cantidadDisponible: Int?,
    val nivelCritico: Int?,
)

data class NotificacionReciente(
    val idNotificacion: String,
    val asunto: String?,
    val estadoEnvio: String?,
    val fechaEnvio: String?,
)

data class DashboardData(
    val role: String?,
    val cards: List<DashboardKpiCard> = emptyList(),
    val lists: DashboardLists? = null,
)

enum class StatsPeriod(val apiValue: String, val label: String) {
    DAY("day", "Día"),
    WEEK("week", "Semana"),
    MONTH("month", "Mes"),
    YEAR("year", "Año"),
}

data class StatsBucket(
    val label: String,
    val count: Int,
)

data class ModuleStats(
    val module: String,
    val period: StatsPeriod,
    val total: Int,
    val buckets: List<StatsBucket> = emptyList(),
)
