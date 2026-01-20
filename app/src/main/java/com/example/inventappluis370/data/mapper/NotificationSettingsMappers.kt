package com.example.inventappluis370.data.mapper

import com.example.inventappluis370.data.model.NotificacionSettings
import com.example.inventappluis370.domain.model.NotificationSettings

fun NotificacionSettings.toDomain(): NotificationSettings = NotificationSettings(
    recibirNotificaciones = recibirNotificaciones ?: true,
    tiposNotificacion = tiposNotificacion ?: emptyList(),
)

fun NotificationSettings.toDto(): NotificacionSettings = NotificacionSettings(
    recibirNotificaciones = recibirNotificaciones,
    tiposNotificacion = tiposNotificacion,
)

