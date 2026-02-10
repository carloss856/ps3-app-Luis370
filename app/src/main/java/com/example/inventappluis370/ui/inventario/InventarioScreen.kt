package com.example.inventappluis370.ui.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.Inventario
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun InventarioScreen(
    navController: NavController,
    viewModel: InventarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Escucha señal de refresco desde el formulario
    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getInventario()
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is InventarioUiState.OperationSuccess) {
            viewModel.getInventario()
        }
    }

    val refreshing = uiState is InventarioUiState.Loading

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Inventario",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Inventory,
                endIconContentDescription = "Inventario",
                onRefresh = { viewModel.getInventario() },
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("inventario/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Entrada")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshContainer(
            refreshing = refreshing,
            onRefresh = { viewModel.getInventario() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is InventarioUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is InventarioUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is InventarioUiState.Success -> {
                    val inventario = state.inventario
                    if (inventario.isEmpty()) {
                        Text(
                            "No hay movimientos de inventario para mostrar.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(inventario) { item ->
                                InventarioItem(
                                    item = item,
                                    onDelete = {
                                        val idEntrada = item.idEntrada
                                        if (!idEntrada.isNullOrBlank()) viewModel.deleteInventario(idEntrada)
                                    },
                                    canDelete = viewModel.canDelete()
                                )
                            }
                        }
                    }
                }

                InventarioUiState.OperationSuccess -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun InventarioItem(item: Inventario, onDelete: () -> Unit, canDelete: Boolean) {
    val idEntrada = item.idEntrada

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                val idRepuesto = item.idRepuesto
                Text(
                    text = "Repuesto ID: ${idRepuesto ?: "(faltante)"}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(text = "Cantidad: +${item.cantidadEntrada?.toString() ?: "(sin dato)"}")

                if (!idEntrada.isNullOrBlank()) {
                    Text(text = "ID Entrada: $idEntrada", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        text = "ERROR: movimiento sin id_entrada (debe corregirse en backend)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                val fecha = item.fechaEntrada
                if (!fecha.isNullOrBlank()) {
                    Text(text = "Fecha: $fecha", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (canDelete && !idEntrada.isNullOrBlank()) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Entrada")
                }
            }
        }
    }
}
