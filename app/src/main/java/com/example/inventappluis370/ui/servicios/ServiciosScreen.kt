package com.example.inventappluis370.ui.servicios

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
                                    onDelete = { if (!id.isNullOrBlank()) viewModel.deleteServicio(id) },
                                    onEdit = { if (!id.isNullOrBlank()) navController.navigate("servicios/$id") },
                                    onViewPartes = { if (!id.isNullOrBlank()) navController.navigate("servicios/$id/partes") },
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
                                        text = "Error cargando más: ${PagingUi.messageOf(error)}",
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
    }
}

@Composable
fun ServicioItem(
    servicio: Servicio,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onViewPartes: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    val id = servicio.idServicio

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                if (!id.isNullOrBlank()) {
                    Text("Servicio: $id", style = MaterialTheme.typography.titleMedium)
                } else {
                    Text(
                        "ERROR: servicio sin id_servicio (debe corregirse en backend)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                val idEquipo = servicio.idEquipo
                if (!idEquipo.isNullOrBlank()) Text("Equipo: $idEquipo")

                val estado = servicio.estado
                if (!estado.isNullOrBlank()) Text("Estado: $estado")
            }
            IconButton(onClick = onViewPartes, enabled = !id.isNullOrBlank()) {
                Icon(Icons.Default.History, contentDescription = "Partes")
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
