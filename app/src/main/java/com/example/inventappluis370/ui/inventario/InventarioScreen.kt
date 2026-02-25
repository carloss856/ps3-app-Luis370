package com.example.inventappluis370.ui.inventario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
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
import com.example.inventappluis370.data.model.Inventario
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer
import com.example.inventappluis370.ui.repuestos.RepuestosUiState
import com.example.inventappluis370.ui.repuestos.RepuestosViewModel

@Composable
fun InventarioScreen(
    navController: NavController,
    viewModel: InventarioViewModel = hiltViewModel(),
    repuestosViewModel: RepuestosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val repuestosState by repuestosViewModel.uiState.collectAsState()

    var selectedItem by remember { mutableStateOf<Inventario?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var operationMessage by remember { mutableStateOf<String?>(null) }

    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getInventario()
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }

    LaunchedEffect(Unit) {
        if (repuestosState !is RepuestosUiState.Success) repuestosViewModel.getRepuestos()
        val msg = navController.currentBackStackEntry?.savedStateHandle?.get<String>("operation_message")
        if (!msg.isNullOrBlank()) {
            operationMessage = msg
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("operation_message")
        }
    }

    val repuestoById = (repuestosState as? RepuestosUiState.Success)
        ?.repuestos
        ?.associateBy({ it.idRepuesto ?: "" }, { it.nombreRepuesto ?: (it.idRepuesto ?: "") })
        .orEmpty()

    val refreshing = uiState is InventarioUiState.Loading

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Inventario",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Inventory,
                endIconContentDescription = "Inventario",
                onRefresh = null,
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("inventario/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Anadir Entrada")
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
                                    repuestoName = repuestoById[item.idRepuesto.orEmpty()],
                                    onView = { selectedItem = item },
                                    onDelete = {
                                        val idEntrada = item.idEntrada
                                        if (!idEntrada.isNullOrBlank()) pendingDeleteId = idEntrada
                                    },
                                    canDelete = viewModel.canDelete()
                                )
                            }
                        }
                    }
                }

                InventarioUiState.OperationSuccess -> {
                }
            }
        }

        if (selectedItem != null) {
            val i = selectedItem!!
            AlertDialog(
                onDismissRequest = { selectedItem = null },
                confirmButton = { TextButton(onClick = { selectedItem = null }) { Text("Cerrar") } },
                title = { Text("Detalle de inventario") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val repuesto = repuestoById[i.idRepuesto.orEmpty()] ?: i.idRepuesto ?: "-"
                        Text("Repuesto: $repuesto")
                        Text("Cantidad entrada: ${i.cantidadEntrada ?: "-"}")
                        Text("Fecha: ${i.fechaEntrada ?: "-"}")
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
                            viewModel.deleteInventario(id)
                            operationMessage = "Entrada eliminada correctamente"
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text("Cancelar") } },
                title = { Text("Confirmar eliminacion") },
                text = { Text("Deseas eliminar este movimiento?") }
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
fun InventarioItem(
    item: Inventario,
    repuestoName: String?,
    onView: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    val idEntrada = item.idEntrada

    Card(modifier = Modifier.fillMaxWidth(), onClick = onView) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                val repuesto = repuestoName ?: item.idRepuesto ?: "(faltante)"
                Text(
                    text = "Repuesto: $repuesto",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(text = "Cantidad: +${item.cantidadEntrada?.toString() ?: "(sin dato)"}")

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

