package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modelos para endpoints /api/permissions.
 *
 * IMPORTANTE: el backend suele devolver un objeto "rico" con:
 * - effective: { role, modules, routes }
 * - override:  { modules, routes } | null
 *
 * Pero para compatibilidad puede devolver también el shape "plano":
 * - { role, modules, routes }
 */
@JsonClass(generateAdapter = true)
data class PermissionsMatrix(
    @Json(name = "role") val role: String? = null,
    /** actions por moduleKey, p.ej {"servicios":["index","store"]} */
    @Json(name = "modules") val modules: Map<String, List<String>>? = null,
    /** nombres de ruta permitidas/override (opcional según backend) */
    @Json(name = "routes") val routes: List<String>? = null,
)

@JsonClass(generateAdapter = true)
data class PermissionsResponse(
    /** backend puede mandar schemaVersion o schema_version */
    @Json(name = "schemaVersion") val schemaVersion: Int? = null,
    @Json(name = "schema_version") val schemaVersionSnake: Int? = null,

    /** shape plano (compat) */
    @Json(name = "role") val role: String? = null,
    @Json(name = "modules") val modules: Map<String, List<String>>? = null,
    @Json(name = "routes") val routes: List<String>? = null,

    /** shape rico (preferido) */
    @Json(name = "effective") val effective: PermissionsMatrix? = null,
    @Json(name = "override") val override: PermissionsMatrix? = null,
) {
    /** Schema version resolvida, si viene por cualquiera de los 2 nombres. */
    val resolvedSchemaVersion: Int? get() = schemaVersion ?: schemaVersionSnake

    /** Matriz efectiva preferida (effective), si no usa el shape plano. */
    val resolvedEffective: PermissionsMatrix
        get() = effective ?: PermissionsMatrix(role = role, modules = modules, routes = routes)

    /**
     * Para edición en UI:
     * - Si el backend trae shape rico con `override` PRESENTE pero sin `modules`, eso normalmente significa
     *   que existe documento override pero aún no define módulos (o viene parcial). En ese caso, tomamos
     *   `effective.modules` como base para no mostrar la pantalla vacía.
     * - Si `override.modules` existe, editamos eso.
     * - Si no hay shape rico, usamos `modules` del shape plano.
     */
    fun resolvedEditableModules(): Map<String, List<String>> {
        val overrideModules = override?.modules
        if (override != null && overrideModules == null) {
            return effective?.modules ?: modules ?: emptyMap()
        }
        return overrideModules
            ?: effective?.modules
            ?: modules
            ?: emptyMap()
    }
}

/**
 * Request para PUT /permissions (override completo/parcial).
 * En web se trabaja por módulos y acciones.
 */
@JsonClass(generateAdapter = true)
data class PermissionsOverrideRequest(
    @Json(name = "modules") val modules: Map<String, List<String>>
)
