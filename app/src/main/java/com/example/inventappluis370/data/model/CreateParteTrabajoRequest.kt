package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Payload para crear un parte de trabajo en un servicio.
 */
data class CreateParteTrabajoRequest(
    @field:Json(name = "tipo_tarea")
    val tipoTarea: String,
    
    val minutos: Int,
    val notas: String?
)
