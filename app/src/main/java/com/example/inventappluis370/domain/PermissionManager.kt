package com.example.inventappluis370.domain

object PermissionManager {

    private fun normalizeRole(userRole: String?): String {
        return userRole
            ?.trim()
            ?.lowercase()
            ?.replace("á", "a")
            ?.replace("é", "e")
            ?.replace("í", "i")
            ?.replace("ó", "o")
            ?.replace("ú", "u")
            ?: ""
    }

    private fun isAdmin(userRole: String?): Boolean {
        val role = normalizeRole(userRole)
        return role == "administrador" || role == "admin"
    }

    private fun isGerente(userRole: String?): Boolean = normalizeRole(userRole) == "gerente"
    private fun isTecnico(userRole: String?): Boolean = normalizeRole(userRole) == "tecnico"
    private fun isCliente(userRole: String?): Boolean = normalizeRole(userRole) == "cliente"

    fun canCreate(userRole: String?, module: String): Boolean {
        return when (module) {
            "Empresas" -> isAdmin(userRole)
            "Usuarios" -> isAdmin(userRole)
            "Equipos" -> isAdmin(userRole) || isTecnico(userRole)
            "Servicios" -> isAdmin(userRole) || isTecnico(userRole)
            "Garantías" -> isAdmin(userRole) || isTecnico(userRole)
            "Repuestos" -> isAdmin(userRole)
            "Inventario" -> isAdmin(userRole) || isTecnico(userRole)
            "SolicitudRepuestos" -> isAdmin(userRole) || isTecnico(userRole) || isCliente(userRole)
            "Notificaciones" -> isAdmin(userRole) || isTecnico(userRole)
            "Reportes" -> isAdmin(userRole) || isGerente(userRole)
            "RMA" -> isAdmin(userRole)
            "Tarifas" -> isAdmin(userRole)
            "Permisos" -> isAdmin(userRole)
            else -> false
        }
    }

    fun canUpdate(userRole: String?, module: String): Boolean {
        return when (module) {
            "Empresas" -> isAdmin(userRole) || isGerente(userRole)
            "Usuarios" -> isAdmin(userRole) || isGerente(userRole)
            "Equipos" -> isAdmin(userRole) || isTecnico(userRole)
            "Servicios" -> isAdmin(userRole) || isTecnico(userRole)
            "Garantías" -> isAdmin(userRole) || isTecnico(userRole)
            "Repuestos" -> isAdmin(userRole)
            "Inventario" -> isAdmin(userRole)
            "SolicitudRepuestos" -> isAdmin(userRole) || isTecnico(userRole) || isGerente(userRole)
            "Notificaciones" -> isAdmin(userRole)
            "Reportes" -> isAdmin(userRole) || isGerente(userRole)
            "RMA" -> isAdmin(userRole)
            "Tarifas" -> isAdmin(userRole)
            "Permisos" -> isAdmin(userRole)
            else -> false
        }
    }

    fun canDelete(userRole: String?, module: String): Boolean {
        return when (module) {
            "Empresas" -> isAdmin(userRole)
            "Usuarios" -> isAdmin(userRole)
            "Equipos" -> isAdmin(userRole)
            "Servicios" -> isAdmin(userRole) || isTecnico(userRole)
            "Garantías" -> isAdmin(userRole)
            "Repuestos" -> isAdmin(userRole)
            "Inventario" -> isAdmin(userRole)
            "SolicitudRepuestos" -> isAdmin(userRole)
            "Notificaciones" -> isAdmin(userRole)
            "Reportes" -> isAdmin(userRole) || isGerente(userRole)
            "RMA" -> isAdmin(userRole)
            "Tarifas" -> isAdmin(userRole)
            "Permisos" -> isAdmin(userRole)
            else -> false
        }
    }
}
