package com.example.inventappluis370.ui.autenticacionusuarios

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.AutenticacionUsuario
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun AutenticacionUsuariosScreen(
    navController: NavController,
    viewModel: AutenticacionUsuariosViewModel = hiltViewModel<AutenticacionUsuariosViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()

    val refreshing = uiState is AutenticacionUsuariosUiState.Loading

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Autenticación - Usuarios",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Lock,
                endIconContentDescription = "Autenticación",
            )
        }
    ) { paddingValues ->
        PullToRefreshContainer(
            refreshing = refreshing,
            onRefresh = { viewModel.getAutenticaciones() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Este módulo muestra registros de autenticación (inicios de sesión / eventos de acceso) para auditoría y control. No modifica usuarios; solo consulta y refresca la información.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = uiState) {
                        is AutenticacionUsuariosUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        is AutenticacionUsuariosUiState.Error -> {
                            Text(
                                text = "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        is AutenticacionUsuariosUiState.Success -> {
                            val itemsList = state.items
                            if (itemsList.isEmpty()) {
                                Text("No hay registros.", modifier = Modifier.align(Alignment.Center))
                            } else {
                                // TEMP: UI mínima para estabilizar compilación.
                                Text(
                                    text = "Registros cargados: ${itemsList.size}",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutenticacionUsuarioItem(item: AutenticacionUsuario) {
    val correo = item.email ?: ""
    val estado = item.estado ?: ""
    val idUsuario = item.idUsuario ?: ""
    val codigo = item.codigoUsuario ?: ""
    val fecha = item.fechaCreacion ?: ""

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (correo.isNotBlank()) correo else "(Sin email)",
                style = MaterialTheme.typography.titleMedium
            )

            if (estado.isNotBlank()) {
                Text(text = "Estado: $estado", style = MaterialTheme.typography.bodySmall)
            }
            if (idUsuario.isNotBlank()) {
                Text(text = "ID usuario: $idUsuario", style = MaterialTheme.typography.bodySmall)
            }
            if (codigo.isNotBlank()) {
                Text(text = "Código: $codigo", style = MaterialTheme.typography.bodySmall)
            }
            if (fecha.isNotBlank()) {
                Text(text = "Fecha creación: $fecha", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
