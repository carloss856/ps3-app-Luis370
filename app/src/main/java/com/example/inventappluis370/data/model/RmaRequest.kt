package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Representa el cuerpo de la peticion para crear una nueva entrada de RMA.
 */
data class RmaRequest(
    @Json(name = "id_persona")
    val personaId: String,

    @Json(name = "fecha_creacion")
    val fechaCreacion: String,
    
    // Asumiendo que tambien se necesita una descripcion del producto para la creacion.
    @Json(name = "producto_descripcion")
    val productoDescripcion: String,
    
    val estado: String
)

