package com.example.inventappluis370.ui.inventario

import androidx.compose.foundation.layout.*
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

@Composable
fun InventarioScreen(
    navController: NavController,
    viewModel: InventarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is InventarioUiState.OperationSuccess) {
            viewModel.getInventario()
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Inventario",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Inventory,
                endIconContentDescription = "Inventario"
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                            // TEMP: UI mínima para estabilizar compilación.
                            Text(
                                text = "Movimientos cargados: ${inventario.size}",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    InventarioUiState.OperationSuccess -> {
                        // UI ya manejada por LaunchedEffect(uiState)
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
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
