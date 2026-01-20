package com.example.inventappluis370.domain.model

/**
 * Modelo de dominio para configuración de notificaciones del usuario.
 *
 * Contrato:
 * - recibir_notificaciones: boolean
 * - tipos_notificacion: array<string>
 */
data class NotificationSettings(
    val recibirNotificaciones: Boolean,
    val tiposNotificacion: List<String>,
)

