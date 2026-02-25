package com.example.inventappluis370.ui.solicitudes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.SolicitudRepuesto
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun SolicitudesRepuestoScreen(
    navController: NavController,
    viewModel: SolicitudesRepuestoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedSolicitud by remember { mutableStateOf<SolicitudRepuesto?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var operationMessage by remember { mutableStateOf<String?>(null) }

    val refreshing = uiState is SolicitudesUiState.Loading
    LaunchedEffect(Unit) {
        val msg = navController.currentBackStackEntry?.savedStateHandle?.get<String>("operation_message")
        if (!msg.isNullOrBlank()) {
            operationMessage = msg
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("operation_message")
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Solicitudes de Repuesto",
                titleStyle = MaterialTheme.typography.titleMedium,
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Assignment,
                endIconContentDescription = "Solicitudes",
                onRefresh = null,
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("create-solicitud") }) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Solicitud")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            PullToRefreshContainer(
                refreshing = refreshing,
                onRefresh = { viewModel.getSolicitudes() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is SolicitudesUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is SolicitudesUiState.Error -> {
                        Text(
                            "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is SolicitudesUiState.Success -> {
                        val solicitudes = state.solicitudes
                        if (solicitudes.isEmpty()) {
                            Text("No hay solicitudes para mostrar.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(solicitudes) { solicitud ->
                                    val id = solicitud.idSolicitud
                                    SolicitudItem(
                                        solicitud = solicitud,
                                        onDelete = { if (!id.isNullOrBlank()) pendingDeleteId = id },
                                        onEdit = { if (!id.isNullOrBlank()) navController.navigate("solicitudes/$id") },
                                        onView = { selectedSolicitud = solicitud },
                                        canUpdate = viewModel.canUpdate(),
                                        canDelete = viewModel.canDelete()
                                    )
                                }
                            }
                        }
                    }

                    SolicitudesUiState.OperationSuccess -> {
                        LaunchedEffect(Unit) { viewModel.getSolicitudes() }
                    }
                }
            }
        }

        if (selectedSolicitud != null) {
            val s = selectedSolicitud!!
            AlertDialog(
                onDismissRequest = { selectedSolicitud = null },
                confirmButton = { TextButton(onClick = { selectedSolicitud = null }) { Text("Cerrar") } },
                title = { Text("Detalle de solicitud") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Repuesto: ${s.idRepuesto ?: "-"}")
                        Text("Servicio: ${s.idServicio ?: "-"}")
                        Text("Usuario: ${s.idUsuario ?: "-"}")
                        Text("Cantidad: ${s.cantidadSolicitada ?: "-"}")
                        Text("Estado: ${s.estadoSolicitud ?: "-"}")
                        Text("Comentarios: ${s.comentarios ?: "-"}")
                    }
                }
            )
        }

        if (pendingDeleteId != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteId = null },
                confirmButton = {
                    TextButton(onClick = {
                        val id = pendingDeleteId
                        pendingDeleteId = null
                        if (!id.isNullOrBlank()) {
                            viewModel.deleteSolicitud(id)
                            operationMessage = "Solicitud eliminada correctamente"
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text("Cancelar") } },
                title = { Text("Confirmar eliminacion") },
                text = { Text("?Deseas eliminar esta solicitud?") }
            )
        }

        if (operationMessage != null) {
            AlertDialog(
                onDismissRequest = { operationMessage = null },
                confirmButton = { TextButton(onClick = { operationMessage = null }) { Text("Aceptar") } },
                title = { Text("Operacion completada") },
                text = { Text(operationMessage!!) }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SolicitudItem(
    solicitud: SolicitudRepuesto,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onView) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Solicitud de repuesto",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (canUpdate) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Repuesto: ${solicitud.idRepuesto}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Cantidad: ${solicitud.cantidadSolicitada}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            // EstadoSolicitudBadge eliminado temporalmente para destrabar compilacion.
        }
    }
}

// EstadoSolicitudBadge eliminado temporalmente.

