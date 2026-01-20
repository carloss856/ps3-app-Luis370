package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la entidad Empresa según el contrato.
 *
 * Nota: id_empresa debería venir siempre. Si llega null, es un problema de datos/backend.
 * Aquí lo toleramos para no romper el parseo y poder mostrar/diagnosticar.
 */
@JsonClass(generateAdapter = true)
data class Empresa(
    @field:Json(name = "id_empresa")
    val idEmpresa: String? = null,

    @field:Json(name = "nombre_empresa")
    val nombreEmpresa: String? = null,

    val direccion: String? = null,
    val telefono: String? = null,
    val email: String? = null,

    @field:Json(name = "fecha_creacion")
    val fechaCreacion: String? = null
)
