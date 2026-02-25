package com.example.inventappluis370.ui.empresas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
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
import com.example.inventappluis370.data.model.Empresa
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun EmpresasScreen(
    navController: NavController,
    viewModel: EmpresasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedEmpresa by remember { mutableStateOf<Empresa?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var operationMessage by remember { mutableStateOf<String?>(null) }

    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getEmpresas()
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

    LaunchedEffect(uiState) {
        if (uiState is EmpresasUiState.OperationSuccess) {
            viewModel.getEmpresas()
        }
    }

    val refreshing = uiState is EmpresasUiState.Loading

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Empresas",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Business,
                endIconContentDescription = "Empresas"
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("empresas/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Anadir Empresa")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            PullToRefreshContainer(
                refreshing = refreshing,
                onRefresh = { viewModel.getEmpresas() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is EmpresasUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is EmpresasUiState.Error -> {
                        Text(
                            "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is EmpresasUiState.Success -> {
                        val empresas = state.empresas
                        if (empresas.isEmpty()) {
                            Text("No hay empresas para mostrar.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(empresas.size) { index ->
                                    val empresa = empresas[index]
                                    EmpresaItem(
                                        empresa = empresa,
                                        onDelete = {
                                            val id = empresa.idEmpresa
                                            if (!id.isNullOrBlank()) pendingDeleteId = id
                                        },
                                        onEdit = {
                                            val id = empresa.idEmpresa
                                            if (!id.isNullOrBlank()) navController.navigate("empresas/$id")
                                        },
                                        onView = { selectedEmpresa = empresa },
                                        canUpdate = viewModel.canUpdate() && !empresa.idEmpresa.isNullOrBlank(),
                                        canDelete = viewModel.canDelete() && !empresa.idEmpresa.isNullOrBlank()
                                    )
                                }
                            }
                        }
                    }

                    EmpresasUiState.OperationSuccess -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }

        if (selectedEmpresa != null) {
            val e = selectedEmpresa!!
            AlertDialog(
                onDismissRequest = { selectedEmpresa = null },
                confirmButton = { TextButton(onClick = { selectedEmpresa = null }) { Text("Cerrar") } },
                title = { Text("Detalle de empresa") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Nombre: ${e.nombreEmpresa ?: "-"}")
                        Text("Direccion: ${e.direccion ?: "-"}")
                        Text("Telefono: ${e.telefono ?: "-"}")
                        Text("Email: ${e.email ?: "-"}")
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
                            viewModel.deleteEmpresa(id)
                            operationMessage = "Empresa eliminada correctamente"
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text("Cancelar") } },
                title = { Text("Confirmar eliminacion") },
                text = { Text("Deseas eliminar esta empresa?") }
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
fun EmpresaItem(
    empresa: Empresa,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    val displayName = empresa.nombreEmpresa ?: "(Sin nombre)"

    Card(modifier = Modifier.fillMaxWidth(), onClick = onView) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(displayName, style = MaterialTheme.typography.titleMedium)
                empresa.email?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                empresa.telefono?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
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

