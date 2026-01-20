package com.example.inventappluis370.data.model

import com.squareup.moshi.Json

/**
 * Payload para editar un parte de trabajo.
 */
data class UpdateParteTrabajoRequest(
    val minutos: Int,
    val notas: String?
)
