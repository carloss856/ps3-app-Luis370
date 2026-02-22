package com.example.inventappluis370.ui.common

/**
 * Estado simple para inputs de formularios (value + error).
 * Usado para marcar visualmente campos obligatorios.
 */
data class FormFieldState(
    val value: String = "",
    val error: String? = null
)
