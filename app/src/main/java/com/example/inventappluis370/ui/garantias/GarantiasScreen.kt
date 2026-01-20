package com.example.inventappluis370.ui.garantias

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Verified
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
import com.example.inventappluis370.data.model.Garantia
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun GarantiasScreen(
    navController: NavController,
    viewModel: GarantiasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val refreshing = uiState is GarantiasUiState.Loading

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Garantías",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Verified,
                endIconContentDescription = "Garantías"
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("garantias/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Garantía")
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
                            Text("No hay garantías para mostrar.", modifier = Modifier.align(Alignment.Center))
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
                                        onDelete = { if (!id.isNullOrBlank()) viewModel.deleteGarantia(id) },
                                        onEdit = { if (!id.isNullOrBlank()) navController.navigate("garantias/$id") },
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
    }
}

@Composable
fun GarantiaItem(garantia: Garantia, onDelete: () -> Unit, onEdit: () -> Unit, canUpdate: Boolean, canDelete: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Servicio #${garantia.servicioId}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "ID: ${garantia.idGarantia}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Vence: ${garantia.fechaFin}")
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
