package com.example.inventappluis370.domain.model

/**
 * Modelo de dominio para usuario autenticado.
 *
 * - `idPersona`: ID de negocio (USR-...).
 * - `mongoId`: _id de Mongo (string ObjectId). Se conserva por compatibilidad, pero
 *   para /usuarios/{id}/notificaciones se recomienda id_persona.
 */
data class User(
    val idPersona: String?,
    val mongoId: String?,
    val nombre: String?,
    val email: String?,
    val telefono: String?,
    val tipo: String?,
    val rol: String?,
    val idEmpresa: String?,
    val validadoPorGerente: Boolean?,
    val recibirNotificaciones: Boolean?,
    val tiposNotificacion: List<String>?,
)

