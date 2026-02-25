package com.example.inventappluis370.data.model

/**
 * Representa el cuerpo de la peticion para solicitar un token de reseteo de contrasena.
 */
data class ForgotPasswordRequest(
    val email: String
)

/**
 * Representa el cuerpo de la peticion para verificar el token de reseteo.
 */
data class VerifyTokenRequest(
    val email: String,
    val token: String
)

/**
 * Representa el cuerpo de la peticion para establecer la nueva contrasena.
 */
data class ResetPasswordRequest(
    val email: String,
    val token: String,
    val contrasena: String
)

