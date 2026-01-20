package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Representa el cuerpo de la petición para crear una nueva entrada de RMA.
 */
data class RmaRequest(
    @Json(name = "id_persona")
    val personaId: String,

    @Json(name = "fecha_creacion")
    val fechaCreacion: String,
    
    // Asumiendo que también se necesita una descripción del producto para la creación.
    @Json(name = "producto_descripcion")
    val productoDescripcion: String,
    
    val estado: String
)
