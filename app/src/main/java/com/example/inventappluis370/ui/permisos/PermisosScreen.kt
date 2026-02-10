package com.example.inventappluis370.ui.permisos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inventappluis370.ui.common.ModuleTopBar

/**
 * Pantalla mínima de Permisos (RBAC).
 *
 * TODO: Conectar a endpoints /api/permissions (GET/PUT/RESET) cuando el backend esté listo
 * y persistir un override como en web.
 */
@Composable
fun PermisosScreen(navController: NavController) {
    val modules = listOf(
        "tarifas-servicio",
        "empresas",
        "usuarios",
        "equipos",
        "servicios",
        "repuestos",
        "inventario",
        "solicitud-repuestos",
        "notificaciones",
        "reportes",
        "rma",
    )

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            ModuleTopBar(
                title = "Permisos",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Security,
                endIconContentDescription = "Permisos",
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Pendiente: editor de RBAC como en web. Esta pantalla está creada para completar el menú.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }

                items(modules) { key ->
                    Text(text = "• $key", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
