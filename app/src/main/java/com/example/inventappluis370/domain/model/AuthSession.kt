package com.example.inventappluis370.domain.model

/**
 * Sesion de autenticacion (dominio).
 *
 * Contrato:
 * - token: Bearer token (string plano)
 * - expiresAt: ISO-8601 (string)
 * - role: el backend usa `tipo` (Administrador|Tecnico|Gerente|Cliente|Empresa)
 * - userId: id_persona (USR-...) cuando existe.
 * - mongoUserId: _id de Mongo (compatibilidad; no deberia ser necesario para configuracion).
 */
data class AuthSession(
    val token: String,
    val expiresAt: String?,
    val role: String?,
    val userId: String?,
    val mongoUserId: String?,
)


