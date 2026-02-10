package com.example.inventappluis370.ui.notificaciones

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.Notificacion
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun NotificacionesScreen(
    navController: NavController,
    viewModel: NotificacionesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val refreshing = uiState is NotificacionesUiState.Loading

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Notificaciones",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Notifications,
                endIconContentDescription = "Notificaciones"
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            PullToRefreshContainer(
                refreshing = refreshing,
                onRefresh = { viewModel.getNotificaciones() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is NotificacionesUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is NotificacionesUiState.Error -> {
                        Text(
                            "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is NotificacionesUiState.Success -> {
                        val notificaciones = state.notificaciones
                        if (notificaciones.isEmpty()) {
                            Text("No hay notificaciones.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Button(
                                        onClick = { viewModel.marcarTodasLeidas() },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("Leer todas")
                                    }
                                }

                                items(notificaciones) { notificacion ->
                                    val id = notificacion.idNotificacion
                                    NotificacionItem(
                                        notificacion = notificacion,
                                        onMarkRead = {
                                            if (!id.isNullOrBlank()) viewModel.setLeida(id)
                                        },
                                        onDelete = { if (!id.isNullOrBlank()) viewModel.deleteNotificacion(id) },
                                        canDelete = viewModel.canDelete()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacionItem(
    notificacion: Notificacion,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean,
) {
    val id = notificacion.idNotificacion
    val isRead = notificacion.leida == true

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isRead && !id.isNullOrBlank()) {
                onMarkRead()
            }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notificacion.asunto ?: "(Sin asunto)",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isRead,
                        onCheckedChange = { checked ->
                            // Backend 9.9 define marcar leída. "Desmarcar" no está garantizado.
                            if (checked && !isRead && !id.isNullOrBlank()) onMarkRead()
                        },
                        enabled = !isRead && !id.isNullOrBlank(),
                    )
                }

                notificacion.tipo?.takeIf { it.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Tipo: $it", style = MaterialTheme.typography.bodySmall)
                }

                Text(text = notificacion.mensaje ?: "")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enviado a: ${notificacion.emailDestinatario ?: ""}",
                    style = MaterialTheme.typography.bodySmall
                )

                if (!id.isNullOrBlank()) {
                    Text(
                        text = "ID: $id",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = "ERROR: notificación sin id_notificacion (debe corregirse en backend)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (canDelete && !id.isNullOrBlank()) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Notificación")
                }
            }
        }
    }
}
