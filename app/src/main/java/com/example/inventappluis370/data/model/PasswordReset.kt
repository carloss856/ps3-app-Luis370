package com.example.inventappluis370.data.model

/**
 * Representa el cuerpo de la petición para solicitar un token de reseteo de contraseña.
 */
data class ForgotPasswordRequest(
    val email: String
)

/**
 * Representa el cuerpo de la petición para verificar el token de reseteo.
 */
data class VerifyTokenRequest(
    val email: String,
    val token: String
)

/**
 * Representa el cuerpo de la petición para establecer la nueva contraseña.
 */
data class ResetPasswordRequest(
    val email: String,
    val token: String,
    val contrasena: String
)
