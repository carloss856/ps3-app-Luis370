package com.example.inventappluis370.data.model

import com.example.inventappluis370.core.network.LenientString
import com.example.inventappluis370.core.network.MongoId
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO Usuario según contrato (usado en /login y /usuarios).
 *
 * Nota:
 * - `idPersona` (id_persona) es el ID recomendado para rutas como /usuarios/{id}/notificaciones.
 * - `mongoId` (_id) se mantiene por compatibilidad cuando el backend lo envía.
 * - Algunos backends envían el ObjectId como `_id`, otros como `id` o `userId`.
 */
@JsonClass(generateAdapter = true)
data class Usuario(
    @field:Json(name = "id_persona")
    val idPersona: String? = null,

    // Campo canónico: `_id`.
    @MongoId
    @field:Json(name = "_id")
    val mongoIdRaw: String? = null,

    // Fallbacks comunes cuando el backend no usa `_id`.
    @MongoId
    @field:Json(name = "id")
    val idRaw: String? = null,

    @MongoId
    @field:Json(name = "userId")
    val userIdRaw: String? = null,

    val nombre: String? = null,
    val email: String? = null,

    @LenientString
    val telefono: String? = null,

    val tipo: String? = null,

    @field:Json(name = "id_empresa")
    val idEmpresa: String? = null,

    @field:Json(name = "validado_por_gerente")
    val validadoPorGerente: Boolean? = null,

    @field:Json(name = "recibir_notificaciones")
    val recibirNotificaciones: Boolean? = null,

    @field:Json(name = "tipos_notificacion")
    val tiposNotificacion: List<String>? = null
) {
    /** Id de Mongo resuelto (prioridad: `_id` -> `id` -> `userId`). */
    fun mongoIdResolved(): String? = mongoIdRaw ?: idRaw ?: userIdRaw
}
