package com.example.inventappluis370.ui.garantias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Verified
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
import com.example.inventappluis370.data.model.Garantia
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun GarantiasScreen(
    navController: NavController,
    viewModel: GarantiasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedGarantia by remember { mutableStateOf<Garantia?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var operationMessage by remember { mutableStateOf<String?>(null) }

    val refreshing = uiState is GarantiasUiState.Loading

    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getGarantias()
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
                title = "Garantias",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Verified,
                endIconContentDescription = "Garantias"
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("garantias/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Anadir Garantia")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            PullToRefreshContainer(
                refreshing = refreshing,
                onRefresh = { viewModel.getGarantias() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is GarantiasUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is GarantiasUiState.Error -> {
                        Text(
                            "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is GarantiasUiState.Success -> {
                        val garantias = state.garantias
                        if (garantias.isEmpty()) {
                            Text("No hay garantias para mostrar.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(garantias) { garantia ->
                                    val id = garantia.idGarantia
                                    GarantiaItem(
                                        garantia = garantia,
                                        onDelete = { if (!id.isNullOrBlank()) pendingDeleteId = id },
                                        onEdit = { if (!id.isNullOrBlank()) navController.navigate("garantias/$id") },
                                        onView = { selectedGarantia = garantia },
                                        canUpdate = viewModel.canUpdate(),
                                        canDelete = viewModel.canDelete()
                                    )
                                }
                            }
                        }
                    }

                    GarantiasUiState.OperationSuccess -> {
                        LaunchedEffect(Unit) { viewModel.getGarantias() }
                    }
                }
            }
        }

        if (selectedGarantia != null) {
            val g = selectedGarantia!!
            AlertDialog(
                onDismissRequest = { selectedGarantia = null },
                confirmButton = { TextButton(onClick = { selectedGarantia = null }) { Text("Cerrar") } },
                title = { Text("Detalle de garantia") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Servicio: ${g.servicioId ?: "-"}")
                        Text("Inicio: ${g.fechaInicio ?: "-"}")
                        Text("Fin: ${g.fechaFin ?: "-"}")
                        Text("Observaciones: ${g.observaciones ?: "-"}")
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
                            viewModel.deleteGarantia(id)
                            operationMessage = "Garantia eliminada correctamente"
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text("Cancelar") } },
                title = { Text("Confirmar eliminacion") },
                text = { Text("Deseas eliminar esta garantia?") }
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
fun GarantiaItem(
    garantia: Garantia,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onView) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Garantia de servicio",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Vence: ${garantia.fechaFin ?: "-"}")
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

