package com.example.inventappluis370.ui.navigation

/**
 * Rutas centralizadas para evitar strings duplicados.
 * Mantener estos valores alineados con el NavHost existente.
 */
object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val ESTADISTICAS = "estadisticas"
    const val PASSWORD_RESET = "password-reset"

    const val EMPRESAS = "empresas"
    const val EMPRESAS_NEW = "empresas/new"
    const val EMPRESAS_EDIT = "empresas/{empresaId}"

    const val USUARIOS = "usuarios"
    const val USUARIOS_NEW = "usuarios/new"
    const val USUARIOS_EDIT = "usuarios/{userId}"

    const val SERVICIOS = "servicios"
    const val SERVICIOS_NEW = "servicios/new"
    const val SERVICIOS_EDIT = "servicios/{servicioId}"

    const val SERVICIOS_PARTES = "servicios/{servicioId}/partes"
    const val SERVICIOS_PARTES_NEW = "servicios/{servicioId}/partes/new"
    const val SERVICIOS_PARTES_EDIT = "servicios/{servicioId}/partes/{parteId}"

    const val TARIFAS = "tarifas"
    const val TARIFAS_NEW = "tarifas/new"
    const val TARIFAS_EDIT = "tarifas/{tarifaId}"

    const val PROPIEDAD_EQUIPOS = "propiedad-equipos"
    const val PROPIEDAD_EQUIPOS_NEW = "propiedad-equipos/new"
    const val PROPIEDAD_EQUIPOS_EDIT = "propiedad-equipos/{propiedadId}"

    const val REPUESTOS = "repuestos"
    const val REPUESTOS_NEW = "repuestos/new"
    const val REPUESTOS_EDIT = "repuestos/{repuestoId}"

    const val GARANTIAS = "garantias"
    const val GARANTIAS_NEW = "garantias/new"
    const val GARANTIAS_EDIT = "garantias/{garantiaId}"

    const val SOLICITUDES_REPUESTOS = "solicitudes-repuestos"
    const val SOLICITUDES_REPUESTOS_NEW = "create-solicitud"

    const val EQUIPOS = "equipos"
    const val EQUIPOS_NEW = "equipos/new"
    const val EQUIPOS_EDIT = "equipos/{equipoId}"

    const val INVENTARIO = "inventario"
    const val INVENTARIO_NEW = "inventario/new"

    const val RMA = "rma"

    const val REPORTES = "reportes"
    const val REPORTES_NEW = "reportes/new"

    const val NOTIFICACIONES = "notificaciones"

    const val CONFIG_NOTIFICACIONES = "configuracion/notificaciones"

    const val PERMISOS = "permisos"

    // Autenticación-usuarios (deshabilitado en menú; se deja solo si se necesitara en soporte)
    // const val AUTENTICACION_USUARIOS = "autenticacion-usuarios"
}
