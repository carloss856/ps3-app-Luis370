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
 * Pero para compatibilidad puede devolver tambien el shape "plano":
 * - { role, modules, routes }
 *
 * El campo "modules" tiene DOS formas posibles segun el endpoint:
 * - Plana:    { "servicios": ["index","store"] }                       -> GET /permissions/user/{id}
 * - Por rol:  { "servicios": { "Administrador": ["index","store"] } }   -> GET /permissions (global)
 *
 * Se parsea como Map<String, Any> (valor sin tipar) para no forzar ninguna de las 2 formas.
 * Antes se tipaba como Map<String, List<String>>, lo que activaba un adapter Moshi tolerante
 * (LenientStringListAdapter) que, al toparse con el shape "por rol" (un objeto, no un arreglo),
 * lo descartaba en silencio devolviendo listas vacias para cada modulo -> todos los switches
 * aparecian apagados en modo Global aunque el backend si tuviera acciones habilitadas.
 */
@JsonClass(generateAdapter = true)
data class PermissionsMatrix(
    @Json(name = "role") val role: String? = null,
    @Json(name = "modules") val modules: Map<String, Any>? = null,
    /** nombres de ruta permitidas/override (opcional segun backend) */
    @Json(name = "routes") val routes: List<String>? = null,
) {
    companion object {
        /** Convierte un valor JSON arbitrario (lista, string suelto/CSV, etc.) a List<String>. */
        @Suppress("UNCHECKED_CAST")
        fun asStringList(value: Any?): List<String> = when (value) {
            null -> emptyList()
            is List<*> -> value.mapNotNull { it?.toString() }
            is String -> if (value.isBlank()) {
                emptyList()
            } else {
                value.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            }
            else -> emptyList()
        }
    }
}

@JsonClass(generateAdapter = true)
data class PermissionsResponse(
    /** backend puede mandar schemaVersion o schema_version */
    @Json(name = "schemaVersion") val schemaVersion: Int? = null,
    @Json(name = "schema_version") val schemaVersionSnake: Int? = null,

    /** shape plano (compat) */
    @Json(name = "role") val role: String? = null,
    @Json(name = "modules") val modules: Map<String, Any>? = null,
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
     * Fuente de modulos a editar, respetando la misma prioridad de siempre:
     * - Si hay `override` presente pero sin `modules`, usar `effective.modules` (para no mostrar
     *   la pantalla vacia).
     * - Si `override.modules` existe, usar eso.
     * - Si no hay shape rico, usar `modules` del shape plano.
     */
    private fun resolvedSourceModules(): Map<String, Any> {
        val overrideModules = override?.modules
        return when {
            override != null && overrideModules == null -> effective?.modules ?: modules ?: emptyMap()
            overrideModules != null -> overrideModules
            else -> effective?.modules ?: modules ?: emptyMap()
        }
    }

    /**
     * Para edicion en modo "por usuario" (shape plano: modulo -> [acciones]).
     */
    fun resolvedEditableModules(): Map<String, List<String>> =
        resolvedSourceModules().mapValues { (_, v) -> PermissionsMatrix.asStringList(v) }

    /**
     * Para edicion en modo "Global" (shape por rol: modulo -> rol -> [acciones]).
     * Si el valor de un modulo no es un objeto (p.ej. viniera plano por error), se trata como
     * sin acciones para ese modulo en vez de fallar.
     */
    fun resolvedEditableModulesByRole(): Map<String, Map<String, List<String>>> =
        resolvedSourceModules().mapValues { (_, v) ->
            when (v) {
                is Map<*, *> -> v.entries.associate { (r, actions) ->
                    (r as? String ?: r.toString()) to PermissionsMatrix.asStringList(actions)
                }
                else -> emptyMap()
            }
        }
}

/**
 * Request para PUT /permissions (override completo/parcial).
 *
 * El body tiene forma distinta segun el modo:
 * - Global:      { "servicios": { "Administrador": ["index","store"], "Gerente": [...] } }
 * - Por usuario: { "servicios": ["index","store"] }
 *
 * "modules" se tipa como Map<String, Any> para poder enviar cualquiera de las 2 formas.
 * IMPORTANTE (modo Global): el backend reemplaza el documento de permisos completo con lo que
 * se envie (PermissionsStore::saveOverride no hace merge por rol), asi que hay que mandar
 * siempre la matriz completa de TODOS los roles, no solo el rol que se esta editando, o se
 * perderian los permisos de los demas roles.
 */
@JsonClass(generateAdapter = true)
data class PermissionsOverrideRequest(
    @Json(name = "modules") val modules: Map<String, Any>
)
