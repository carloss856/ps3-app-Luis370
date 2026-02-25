package com.example.inventappluis370.ui.tarifas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
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
import com.example.inventappluis370.data.model.TarifaServicio
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun TarifasScreen(
    navController: NavController,
    viewModel: TarifasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing = uiState is TarifasUiState.Loading

    var selectedTarifa by remember { mutableStateOf<TarifaServicio?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var operationMessage by remember { mutableStateOf<String?>(null) }

    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getTarifas()
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }

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
                title = "Tarifas de Servicio",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.AttachMoney,
                endIconContentDescription = "Tarifas",
                onRefresh = null,
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("tarifas/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Tarifa")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshContainer(
            refreshing = isRefreshing,
            onRefresh = { viewModel.getTarifas() },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TarifasUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                is TarifasUiState.Success -> {
                    if (state.tarifas.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("No hay tarifas configuradas.", modifier = Modifier.align(Alignment.Center))
                        }
                    } else {
                        TarifasList(
                            tarifas = state.tarifas,
                            viewModel = viewModel,
                            onDelete = { id -> pendingDeleteId = id },
                            onEdit = { tarifa -> tarifa.idTarifa?.let { navController.navigate("tarifas/$it") } },
                            onView = { selectedTarifa = it },
                            onViewHistory = { tarifa -> tarifa.idTarifa?.let { navController.navigate("tarifas/$it/historial") } }
                        )
                    }
                }

                is TarifasUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                is TarifasUiState.OperationSuccess -> {
                }
            }
        }

        if (selectedTarifa != null) {
            val t = selectedTarifa!!
            AlertDialog(
                onDismissRequest = { selectedTarifa = null },
                confirmButton = { TextButton(onClick = { selectedTarifa = null }) { Text("Cerrar") } },
                title = { Text("Detalle de tarifa") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Tipo tarea: ${t.tipoTarea ?: "-"}")
                        Text("Nivel tecnico: ${t.nivelTecnico ?: "-"}")
                        Text("Tarifa/hora: ${t.tarifaHora ?: "-"} ${t.moneda ?: ""}")
                        Text("Vigente desde: ${t.vigenteDesde ?: "-"}")
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
                            viewModel.deleteTarifa(id)
                            operationMessage = "Tarifa eliminada correctamente"
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text("Cancelar") } },
                title = { Text("Confirmar eliminacion") },
                text = { Text("Deseas eliminar esta tarifa?") }
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
fun TarifasList(
    tarifas: List<TarifaServicio>,
    viewModel: TarifasViewModel,
    onDelete: (String) -> Unit,
    onEdit: (TarifaServicio) -> Unit,
    onView: (TarifaServicio) -> Unit,
    onViewHistory: (TarifaServicio) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tarifas) { tarifa ->
            TarifaItem(
                tarifa = tarifa,
                onDelete = {
                    tarifa.idTarifa?.let { onDelete(it) }
                },
                onEdit = { onEdit(tarifa) },
                onView = { onView(tarifa) },
                onViewHistory = { onViewHistory(tarifa) },
                canUpdate = viewModel.canUpdate(),
                canDelete = viewModel.canDelete()
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TarifaItem(
    tarifa: TarifaServicio,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    onViewHistory: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onView) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${tarifa.tipoTarea?.uppercase() ?: "SIN TIPO"} - ${tarifa.nivelTecnico ?: "N/A"}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Tarifa: ${tarifa.tarifaHora ?: "N/A"} ${tarifa.moneda ?: ""}")
                tarifa.vigenteDesde?.takeIf { it.isNotBlank() }?.let { Text("Vigente desde: $it", style = MaterialTheme.typography.bodySmall) }
            }

            IconButton(onClick = onViewHistory) {
                Icon(Icons.Default.History, contentDescription = "Historial")
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

