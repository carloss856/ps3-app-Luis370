package com.example.inventappluis370.data.model

import com.example.inventappluis370.core.network.MongoId
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Contrato: AutenticacionUsuario (colección autenticacion_usuarios)
 * Campos según contrato (sin inventar):
 * - id_usuario
 * - codigo_usuario
 * - email
 * - fecha_creacion
 * - intentos_fallidos?
 * - estado?
 */
@JsonClass(generateAdapter = true)
data class AutenticacionUsuario(
    @Json(name = "id_usuario") val idUsuario: String? = null,
    @Json(name = "codigo_usuario") val codigoUsuario: String? = null,
    val email: String? = null,
    @Json(name = "fecha_creacion") val fechaCreacion: String? = null,
    @Json(name = "intentos_fallidos") val intentosFallidos: Int? = null,
    val estado: String? = null,
    @MongoId @Json(name = "_id") val mongoId: String? = null
)
