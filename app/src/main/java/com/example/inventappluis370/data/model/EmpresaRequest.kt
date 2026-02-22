package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Payload para crear o actualizar una empresa.
 *
 * Contrato backend:
 * - requeridos: nombre_empresa, email
 * - opcionales: direccion, telefono, fecha_creacion
 */
@JsonClass(generateAdapter = true)
data class EmpresaRequest(
    @Json(name = "nombre_empresa")
    val nombreEmpresa: String,

    @Json(name = "direccion")
    val direccion: String? = null,

    @Json(name = "telefono")
    val telefono: String? = null,

    @Json(name = "email")
    val email: String,

    // Solo aplica a creación (POST /api/empresas). En update puede omitirse.
    @Json(name = "fecha_creacion")
    val fechaCreacion: String? = null
)
