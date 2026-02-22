package com.example.inventappluis370.ui.common

/**
 * Mapper simple para mensajes de validación del backend (422) a texto legible.
 *
 * El backend a veces retorna strings tipo i18n keys (ej: "validation.required").
 * Para UX en Android, los convertimos a mensajes en español.
 */
object ValidationMessageMapper {

    fun map(raw: String?): String {
        val msg = raw.orEmpty().trim()
        if (msg.isBlank()) return ""

        return when (msg) {
            "validation.required" -> "Este campo es obligatorio"
            "validation.email" -> "Email inválido"
            else -> msg
        }
    }
}
