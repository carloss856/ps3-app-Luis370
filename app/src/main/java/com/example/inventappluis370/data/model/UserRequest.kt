package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Payload para crear o actualizar un usuario.
 */
@JsonClass(generateAdapter = true)
data class UserRequest(
    val nombre: String,
    val email: String,
    val telefono: String?,
    val tipo: String,
    val contrasena: String? = null,

    @Json(name = "id_empresa")
    val idEmpresa: String? = null,

    @Json(name = "validado_por_gerente")
    val validadoPorGerente: Boolean? = null
)
