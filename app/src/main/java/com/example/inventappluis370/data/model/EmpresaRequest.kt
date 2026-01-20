package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Payload para crear o actualizar una empresa.
 */
data class EmpresaRequest(
    @field:Json(name = "nombre_empresa")
    val nombreEmpresa: String,

    val direccion: String?,
    val telefono: String?,
    val email: String
)
