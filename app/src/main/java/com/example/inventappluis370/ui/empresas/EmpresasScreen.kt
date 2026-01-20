package com.example.inventappluis370.ui.empresas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    androidx.compose.runtime.LaunchedEffect(uiState) {
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
                    Icon(Icons.Default.Add, contentDescription = "Añadir Empresa")
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
                            androidx.compose.foundation.lazy.LazyColumn(
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
                                            if (!id.isNullOrBlank()) viewModel.deleteEmpresa(id)
                                        },
                                        onEdit = {
                                            val id = empresa.idEmpresa
                                            if (!id.isNullOrBlank()) navController.navigate("empresas/$id")
                                        },
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
    }
}

@Composable
fun EmpresaItem(
    empresa: Empresa,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    val displayName = empresa.nombreEmpresa ?: "(Sin nombre)"
    val displayId = empresa.idEmpresa

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(displayName, style = MaterialTheme.typography.titleMedium)
                if (!displayId.isNullOrBlank()) {
                    Text("ID: $displayId", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        "ERROR: empresa sin id_empresa (debe corregirse en backend)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
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
