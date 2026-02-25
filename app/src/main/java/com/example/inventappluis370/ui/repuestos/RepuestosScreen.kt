package com.example.inventappluis370.ui.repuestos

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.Repuesto
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun RepuestosScreen(
    navController: NavController,
    viewModel: RepuestosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedRepuesto by remember { mutableStateOf<Repuesto?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var operationMessage by remember { mutableStateOf<String?>(null) }

    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getRepuestos()
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

    val refreshing = uiState is RepuestosUiState.Loading

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Repuestos",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Settings,
                endIconContentDescription = "Repuestos"
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("repuestos/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Anadir Repuesto")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshContainer(
            refreshing = refreshing,
            onRefresh = { viewModel.getRepuestos() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is RepuestosUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is RepuestosUiState.Error -> {
                        Text(
                            "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is RepuestosUiState.Success -> {
                        val repuestos = state.repuestos
                        if (repuestos.isEmpty()) {
                            Text(text = "No hay repuestos para mostrar.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(repuestos) { repuesto ->
                                    val id = repuesto.idRepuesto
                                    RepuestoItem(
                                        repuesto = repuesto,
                                        onDelete = { if (!id.isNullOrBlank()) pendingDeleteId = id },
                                        onEdit = { if (!id.isNullOrBlank()) navController.navigate("repuestos/$id") },
                                        onView = { selectedRepuesto = repuesto },
                                        canUpdate = viewModel.canUpdate(),
                                        canDelete = viewModel.canDelete()
                                    )
                                }
                            }
                        }
                    }

                    RepuestosUiState.OperationSuccess -> {
                        LaunchedEffect(Unit) { viewModel.getRepuestos() }
                    }
                }
            }
        }

        if (selectedRepuesto != null) {
            val r = selectedRepuesto!!
            AlertDialog(
                onDismissRequest = { selectedRepuesto = null },
                confirmButton = { TextButton(onClick = { selectedRepuesto = null }) { Text("Cerrar") } },
                title = { Text("Detalle de repuesto") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Nombre: ${r.nombreRepuesto ?: "-"}")
                        Text("Cantidad: ${r.cantidadDisponible ?: "-"}")
                        Text("Nivel critico: ${r.nivelCritico ?: "-"}")
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
                            viewModel.deleteRepuesto(id)
                            operationMessage = "Repuesto eliminado correctamente"
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text("Cancelar") } },
                title = { Text("Confirmar eliminacion") },
                text = { Text("Deseas eliminar este repuesto?") }
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
fun RepuestoItem(
    repuesto: Repuesto,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    val id = repuesto.idRepuesto

    Card(modifier = Modifier.fillMaxWidth(), onClick = onView) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(repuesto.nombreRepuesto ?: "(Sin nombre)", fontWeight = FontWeight.Bold)
                Text("Cantidad: ${repuesto.cantidadDisponible?.toString() ?: "(sin dato)"}")
                repuesto.nivelCritico?.let { Text("Nivel critico: $it", style = MaterialTheme.typography.bodySmall) }
            }

            if (canUpdate && !id.isNullOrBlank()) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
            }

            if (canDelete && !id.isNullOrBlank()) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

