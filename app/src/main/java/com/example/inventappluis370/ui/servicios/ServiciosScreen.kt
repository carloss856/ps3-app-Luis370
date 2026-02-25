package com.example.inventappluis370.ui.servicios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.inventappluis370.data.model.Servicio
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PagingUi
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun ServiciosScreen(
    navController: NavController,
    viewModel: ServiciosViewModel = hiltViewModel()
) {
    val servicios = viewModel.serviciosPaged.collectAsLazyPagingItems()
    val refreshing = servicios.loadState.refresh is LoadState.Loading

    var selectedServicio by remember { mutableStateOf<Servicio?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var operationMessage by remember { mutableStateOf<String?>(null) }

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
                title = "Servicios",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Build,
                endIconContentDescription = "Servicios"
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("servicios/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Servicio")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            PullToRefreshContainer(
                refreshing = refreshing,
                onRefresh = { servicios.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    refreshing && servicios.itemCount == 0 -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    servicios.loadState.refresh is LoadState.Error -> {
                        val error = (servicios.loadState.refresh as LoadState.Error).error
                        Text(
                            "Error: ${PagingUi.messageOf(error)}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    servicios.itemCount == 0 -> {
                        Text("No hay servicios para mostrar.", modifier = Modifier.align(Alignment.Center))
                    }

                    else -> {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                count = servicios.itemCount,
                                key = servicios.itemKey { it.idServicio ?: "" }
                            ) { index ->
                                val servicio = servicios[index] ?: return@items
                                val id = servicio.idServicio

                                ServicioItem(
                                    servicio = servicio,
                                    onDelete = { if (!id.isNullOrBlank()) pendingDeleteId = id },
                                    onEdit = { if (!id.isNullOrBlank()) navController.navigate("servicios/$id") },
                                    onView = { selectedServicio = servicio },
                                    canUpdate = viewModel.canUpdate() && !id.isNullOrBlank(),
                                    canDelete = viewModel.canDelete() && !id.isNullOrBlank()
                                )
                            }

                            item {
                                if (servicios.loadState.append is LoadState.Loading) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                            }

                            item {
                                if (servicios.loadState.append is LoadState.Error) {
                                    val error = (servicios.loadState.append as LoadState.Error).error
                                    Text(
                                        text = "Error cargando mas: ${PagingUi.messageOf(error)}",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedServicio != null) {
            val s = selectedServicio!!
            AlertDialog(
                onDismissRequest = { selectedServicio = null },
                confirmButton = { TextButton(onClick = { selectedServicio = null }) { Text("Cerrar") } },
                title = { Text("Detalle de servicio") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Equipo: ${s.idEquipo ?: "-"}")
                        Text("Problema: ${s.problemaReportado ?: "-"}")
                        Text("Estado: ${s.estado ?: "-"}")
                        Text("Codigo RMA: ${s.codigoRma ?: "-"}")
                        Text("Fecha ingreso: ${s.fechaIngreso ?: "-"}")
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
                            viewModel.deleteServicio(id)
                            operationMessage = "Servicio eliminado correctamente"
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text("Cancelar") } },
                title = { Text("Confirmar eliminacion") },
                text = { Text("Deseas eliminar este servicio?") }
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
fun ServicioItem(
    servicio: Servicio,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onView) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Servicio", style = MaterialTheme.typography.titleMedium)

                val idEquipo = servicio.idEquipo
                if (!idEquipo.isNullOrBlank()) Text("Equipo: $idEquipo")

                val estado = servicio.estado
                if (!estado.isNullOrBlank()) Text("Estado: $estado")
            }
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
    }
}

