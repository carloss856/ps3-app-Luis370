package com.example.inventappluis370.domain

object PermissionManager {

    fun canCreate(userRole: String?, module: String): Boolean {
        return when (module) {
            "Empresas" -> userRole == "Administrador"
            "Usuarios" -> userRole == "Administrador"
            "Equipos" -> userRole == "Administrador" || userRole == "Técnico"
            "Servicios" -> userRole == "Administrador" || userRole == "Técnico"
            "Garantías" -> userRole == "Administrador" || userRole == "Técnico"
            "Repuestos" -> userRole == "Administrador"
            "Inventario" -> userRole == "Administrador" || userRole == "Técnico"
            "SolicitudRepuestos" -> userRole in listOf("Administrador", "Técnico", "Cliente")
            "Notificaciones" -> userRole == "Administrador" || userRole == "Técnico"
            "Reportes" -> userRole == "Administrador" || userRole == "Gerente"
            "RMA" -> userRole == "Administrador"
            else -> false
        }
    }

    fun canUpdate(userRole: String?, module: String): Boolean {
        return when (module) {
            "Empresas" -> userRole == "Administrador" || userRole == "Gerente"
            "Usuarios" -> userRole == "Administrador" || userRole == "Gerente"
            "Equipos" -> userRole == "Administrador" || userRole == "Técnico"
            "Servicios" -> userRole == "Administrador" || userRole == "Técnico"
            "Garantías" -> userRole == "Administrador" || userRole == "Técnico"
            "Repuestos" -> userRole == "Administrador"
            "Inventario" -> userRole == "Administrador"
            "SolicitudRepuestos" -> userRole in listOf("Administrador", "Técnico", "Gerente")
            "Notificaciones" -> userRole == "Administrador"
            "Reportes" -> userRole == "Administrador" || userRole == "Gerente"
            "RMA" -> userRole == "Administrador"
            else -> false
        }
    }

    fun canDelete(userRole: String?, module: String): Boolean {
        return when (module) {
            "Empresas" -> userRole == "Administrador"
            "Usuarios" -> userRole == "Administrador"
            "Equipos" -> userRole == "Administrador"
            "Servicios" -> userRole == "Administrador" || userRole == "Técnico"
            "Garantías" -> userRole == "Administrador"
            "Repuestos" -> userRole == "Administrador"
            "Inventario" -> userRole == "Administrador"
            "SolicitudRepuestos" -> userRole == "Administrador"
            "Notificaciones" -> userRole == "Administrador"
            "Reportes" -> userRole == "Administrador" || userRole == "Gerente"
            "RMA" -> userRole == "Administrador"
            else -> false
        }
    }
}
