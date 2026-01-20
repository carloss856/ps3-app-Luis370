package com.example.inventappluis370.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.inventappluis370.ui.autenticacionusuarios.AutenticacionUsuariosScreen
import com.example.inventappluis370.ui.configuracion.ConfiguracionScreen
import com.example.inventappluis370.ui.empresas.CreateEditEmpresaScreen
import com.example.inventappluis370.ui.empresas.EmpresasScreen
import com.example.inventappluis370.ui.equipos.CreateEditEquipoScreen
import com.example.inventappluis370.ui.equipos.CreateEditPropiedadScreen
import com.example.inventappluis370.ui.equipos.EquiposScreen
import com.example.inventappluis370.ui.equipos.PropiedadesEquipoScreen
import com.example.inventappluis370.ui.garantias.CreateEditGarantiaScreen
import com.example.inventappluis370.ui.garantias.GarantiasScreen
import com.example.inventappluis370.ui.home.DashboardScreen
import com.example.inventappluis370.ui.inventario.CreateInventarioEntradaScreen
import com.example.inventappluis370.ui.inventario.InventarioScreen
import com.example.inventappluis370.ui.login.AuthViewModel
import com.example.inventappluis370.ui.login.LoginScreen
import com.example.inventappluis370.ui.login.PasswordResetScreen
import com.example.inventappluis370.ui.notificaciones.NotificacionesScreen
import com.example.inventappluis370.ui.reportes.CreateReporteScreen
import com.example.inventappluis370.ui.reportes.ReportesScreen
import com.example.inventappluis370.ui.repuestos.CreateEditRepuestoScreen
import com.example.inventappluis370.ui.repuestos.RepuestosScreen
import com.example.inventappluis370.ui.rma.RmaScreen
import com.example.inventappluis370.ui.servicios.CreateEditParteTrabajoScreen
import com.example.inventappluis370.ui.servicios.CreateEditServicioScreen
import com.example.inventappluis370.ui.servicios.PartesTrabajoScreen
import com.example.inventappluis370.ui.servicios.ServiciosScreen
import com.example.inventappluis370.ui.solicitudes.CreateSolicitudScreen
import com.example.inventappluis370.ui.solicitudes.SolicitudesRepuestoScreen
import com.example.inventappluis370.ui.tarifas.CreateEditTarifaScreen
import com.example.inventappluis370.ui.tarifas.TarifasScreen
import com.example.inventappluis370.ui.usuarios.CreateEditUsuarioScreen
import com.example.inventappluis370.ui.usuarios.UsuariosScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) { LoginScreen(navController) }

        composable(Routes.DASHBOARD) {
            val authViewModel: AuthViewModel = hiltViewModel()
            DashboardScreen(navController = navController, onLogout = { authViewModel.logout() })
        }

        composable(Routes.PASSWORD_RESET) { PasswordResetScreen(navController) }

        // Módulos CRUD
        composable(Routes.EMPRESAS) { EmpresasScreen(navController) }
        composable(Routes.EMPRESAS_NEW) { CreateEditEmpresaScreen(navController) }
        composable(Routes.EMPRESAS_EDIT) { backStackEntry ->
            val empresaId = backStackEntry.arguments?.getString("empresaId")
            CreateEditEmpresaScreen(navController, empresaId = empresaId)
        }

        composable(Routes.USUARIOS) { UsuariosScreen(navController) }
        composable(Routes.USUARIOS_NEW) { CreateEditUsuarioScreen(navController) }
        composable(Routes.USUARIOS_EDIT) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            CreateEditUsuarioScreen(navController, userId = userId)
        }

        composable(Routes.SERVICIOS) { ServiciosScreen(navController) }
        composable(Routes.SERVICIOS_NEW) { CreateEditServicioScreen(navController) }
        composable(Routes.SERVICIOS_EDIT) { backStackEntry ->
            val servicioIdString = backStackEntry.arguments?.getString("servicioId")
            CreateEditServicioScreen(navController, servicioId = servicioIdString)
        }

        // Partes de Trabajo en Servicios
        composable(Routes.SERVICIOS_PARTES) { backStackEntry ->
            val servicioId = backStackEntry.arguments?.getString("servicioId") ?: ""
            PartesTrabajoScreen(navController, servicioId = servicioId)
        }
        composable(Routes.SERVICIOS_PARTES_NEW) { backStackEntry ->
            val servicioId = backStackEntry.arguments?.getString("servicioId") ?: ""
            CreateEditParteTrabajoScreen(navController, servicioId = servicioId)
        }
        composable(Routes.SERVICIOS_PARTES_EDIT) { backStackEntry ->
            val servicioId = backStackEntry.arguments?.getString("servicioId") ?: ""
            val parteId = backStackEntry.arguments?.getString("parteId") ?: ""
            CreateEditParteTrabajoScreen(navController, servicioId = servicioId, parteId = parteId)
        }

        // Tarifas de Servicio
        composable(Routes.TARIFAS) { TarifasScreen(navController) }
        composable(Routes.TARIFAS_NEW) { CreateEditTarifaScreen(navController) }
        composable(Routes.TARIFAS_EDIT) { backStackEntry ->
            val tarifaId = backStackEntry.arguments?.getString("tarifaId")
            CreateEditTarifaScreen(navController, tarifaId = tarifaId)
        }

        // Propiedad de Equipos
        composable(Routes.PROPIEDAD_EQUIPOS) { PropiedadesEquipoScreen(navController) }
        composable(Routes.PROPIEDAD_EQUIPOS_NEW) { CreateEditPropiedadScreen(navController) }
        composable(Routes.PROPIEDAD_EQUIPOS_EDIT) { backStackEntry ->
            val propiedadId = backStackEntry.arguments?.getString("propiedadId")
            CreateEditPropiedadScreen(navController, propiedadId = propiedadId)
        }

        composable(Routes.REPUESTOS) { RepuestosScreen(navController) }
        composable(Routes.REPUESTOS_NEW) { CreateEditRepuestoScreen(navController) }
        composable(Routes.REPUESTOS_EDIT) { backStackEntry ->
            val repuestoId = backStackEntry.arguments?.getString("repuestoId")
            CreateEditRepuestoScreen(navController, repuestoId = repuestoId)
        }

        composable(Routes.GARANTIAS) { GarantiasScreen(navController) }
        composable(Routes.GARANTIAS_NEW) { CreateEditGarantiaScreen(navController) }
        composable(Routes.GARANTIAS_EDIT) { backStackEntry ->
            val garantiaId = backStackEntry.arguments?.getString("garantiaId")
            CreateEditGarantiaScreen(navController, garantiaId = garantiaId)
        }

        composable(Routes.SOLICITUDES_REPUESTOS) { SolicitudesRepuestoScreen(navController) }
        composable(Routes.SOLICITUDES_REPUESTOS_NEW) { CreateSolicitudScreen(navController) }

        composable(Routes.EQUIPOS) { EquiposScreen(navController) }
        composable(Routes.EQUIPOS_NEW) { CreateEditEquipoScreen(navController) }
        composable(Routes.EQUIPOS_EDIT) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getString("equipoId")
            CreateEditEquipoScreen(navController, equipoId = equipoId)
        }

        composable(Routes.INVENTARIO) { InventarioScreen(navController) }
        composable(Routes.INVENTARIO_NEW) { CreateInventarioEntradaScreen(navController) }

        composable(Routes.RMA) { RmaScreen() }

        composable(Routes.REPORTES) { ReportesScreen(navController) }
        composable(Routes.REPORTES_NEW) { CreateReporteScreen(navController) }

        // Módulos de solo lectura
        composable(Routes.NOTIFICACIONES) { NotificacionesScreen(navController) }

        // Configuración
        composable(Routes.CONFIG_NOTIFICACIONES) { ConfiguracionScreen(navController) }

        // Autenticación-usuarios
        composable(Routes.AUTENTICACION_USUARIOS) { AutenticacionUsuariosScreen(navController) }
    }
}
