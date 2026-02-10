package com.example.inventappluis370.domain.usecase

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.inventappluis370.domain.model.DashboardItem
import com.example.inventappluis370.ui.navigation.Routes
import javax.inject.Inject

class GetDashboardItemsUseCase @Inject constructor() {

    operator fun invoke(userRole: String?): List<DashboardItem> {
        val menuItems = mutableListOf<DashboardItem>()

        // 1. Opciones de negocio principales
        menuItems.addAll(getCommonItems())

        // 2. Opciones de alto nivel para Administrador y Gerente
        if (userRole == "Administrador" || userRole == "Gerente") {
            menuItems.addAll(getAdminAndManagerItems())
        }

        // 3. Opciones del "Pie de página" que van al final de la lista
        menuItems.addAll(getFooterItems())

        return menuItems
    }

    private fun getCommonItems(): List<DashboardItem> {
        return listOf(
            DashboardItem("Estadísticas", Icons.Default.Dashboard, Routes.ESTADISTICAS),
            DashboardItem("Empresas", Icons.Default.Business, Routes.EMPRESAS),
            DashboardItem("Equipos", Icons.Default.Computer, Routes.EQUIPOS),
            DashboardItem("Repuestos", Icons.Default.Construction, Routes.REPUESTOS),
            DashboardItem("Servicios", Icons.Default.Build, Routes.SERVICIOS),
            DashboardItem("Solicitudes", Icons.Default.Assignment, Routes.SOLICITUDES_REPUESTOS)
        )
    }

    private fun getAdminAndManagerItems(): List<DashboardItem> {
        return listOf(
            DashboardItem("Inventario", Icons.Default.Inventory, Routes.INVENTARIO),
            DashboardItem("Tarifas", Icons.Default.AttachMoney, Routes.TARIFAS),
            DashboardItem("Garantías", Icons.Default.VerifiedUser, Routes.GARANTIAS),
            DashboardItem("Reportes", Icons.Default.Assessment, Routes.REPORTES),
            DashboardItem("Usuarios", Icons.Default.People, Routes.USUARIOS),
            DashboardItem("Permisos", Icons.Default.AdminPanelSettings, Routes.PERMISOS),
        )
    }

    private fun getFooterItems(): List<DashboardItem> {
        // Agrupamos las opciones finales como lo pediste.
        return listOf(
            DashboardItem("Configuración", Icons.Default.Settings, Routes.CONFIG_NOTIFICACIONES),
        )
    }
}
