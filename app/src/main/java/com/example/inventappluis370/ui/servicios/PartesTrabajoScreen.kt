package com.example.inventappluis370.ui.servicios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.inventappluis370.data.model.ParteTrabajo
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun PartesTrabajoScreen(
    navController: NavController,
    servicioId: String,
    viewModel: PartesTrabajoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val refreshing = uiState is PartesTrabajoUiState.Loading

    LaunchedEffect(servicioId) {
        viewModel.getPartes(servicioId)
    }

    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getPartes(servicioId)
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Partes de Trabajo - Servicio #$servicioId",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        },
        floatingActionButton = {
            if (viewModel.canManage()) {
                FloatingActionButton(onClick = { navController.navigate("servicios/$servicioId/partes/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Parte")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshContainer(
            refreshing = refreshing,
            onRefresh = { viewModel.getPartes(servicioId) },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PartesTrabajoUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PartesTrabajoUiState.Success -> {
                    if (state.partes.isEmpty()) {
                        Text("No hay partes registrados.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        PartesList(
                            partes = state.partes,
                            viewModel = viewModel,
                            servicioId = servicioId,
                            onEdit = {
                                val idParte = it.idParte
                                if (!idParte.isNullOrBlank()) {
                                    navController.navigate("servicios/$servicioId/partes/$idParte")
                                }
                            }
                        )
                    }
                }
                is PartesTrabajoUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                else -> {}
            }
        }
    }
}

@Composable
fun PartesList(partes: List<ParteTrabajo>, viewModel: PartesTrabajoViewModel, servicioId: String, onEdit: (ParteTrabajo) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(partes) { parte ->
            val idParte = parte.idParte
            ParteItem(
                parte = parte,
                onDelete = { if (!idParte.isNullOrBlank()) viewModel.deleteParte(servicioId, idParte) },
                onEdit = { if (!idParte.isNullOrBlank()) onEdit(parte) },
                canManage = viewModel.canManage() && !idParte.isNullOrBlank()
            )
        }
    }
}

@Composable
fun ParteItem(parte: ParteTrabajo, onDelete: () -> Unit, onEdit: () -> Unit, canManage: Boolean) {
    val safeTipo = parte.tipoTarea ?: ""

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (safeTipo.isNotBlank()) safeTipo.uppercase() else "(SIN TIPO)",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = "Duración: ${(parte.minutos ?: 0)} minutos")
                Text(text = "ID: ${parte.idParte ?: "(sin id)"}", style = MaterialTheme.typography.bodySmall)
                if (!parte.notas.isNullOrBlank()) Text(text = "Notas: ${parte.notas}", style = MaterialTheme.typography.bodySmall)
            }
            if (canManage) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}
