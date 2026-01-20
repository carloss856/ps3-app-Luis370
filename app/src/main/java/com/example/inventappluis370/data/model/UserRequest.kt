package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Payload para crear o actualizar un usuario.
 */
data class UserRequest(
    val nombre: String,
    val email: String,
    val telefono: String?,
    val tipo: String,
    val contrasena: String? = null,
    
    @field:Json(name = "id_empresa")
    val idEmpresa: String? = null,
    
    @field:Json(name = "validado_por_gerente")
    val validadoPorGerente: Boolean? = null
)
