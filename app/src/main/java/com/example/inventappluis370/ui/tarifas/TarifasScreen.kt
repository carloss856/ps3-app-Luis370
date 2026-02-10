package com.example.inventappluis370.ui.tarifas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
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
import com.example.inventappluis370.data.model.TarifaServicio
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun TarifasScreen(
    navController: NavController,
    viewModel: TarifasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing = uiState is TarifasUiState.Loading

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    // Escucha señal de refresco de la pantalla de formulario
    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getTarifas()
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Tarifas de Servicio",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.AttachMoney,
                endIconContentDescription = "Tarifas",
                onRefresh = { viewModel.getTarifas() },
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
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.getTarifas() },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TarifasUiState.Loading -> {
                    if (!swipeRefreshState.isRefreshing) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
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
                            onEdit = { tarifa ->
                                tarifa.idTarifa?.let { navController.navigate("tarifas/$it") }
                            }
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
                is TarifasUiState.OperationSuccess -> {}
            }
        }
    }
}

@Composable
fun TarifasList(tarifas: List<TarifaServicio>, viewModel: TarifasViewModel, onEdit: (TarifaServicio) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tarifas) { tarifa ->
            TarifaItem(
                tarifa = tarifa,
                onDelete = { 
                    tarifa.idTarifa?.let { viewModel.deleteTarifa(it) } 
                },
                onEdit = { onEdit(tarifa) },
                canUpdate = viewModel.canUpdate(),
                canDelete = viewModel.canDelete()
            )
        }
    }
}

@Composable
fun TarifaItem(tarifa: TarifaServicio, onDelete: () -> Unit, onEdit: () -> Unit, canUpdate: Boolean, canDelete: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${tarifa.tipoTarea?.uppercase() ?: "SIN TIPO"} - ${tarifa.nivelTecnico ?: "N/A"}", 
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = "${tarifa.tarifaHora ?: 0.0} ${tarifa.moneda ?: "USD"} / hora")
                Text(text = "ID: ${tarifa.idTarifa ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                if (tarifa.activo == false) {
                    Text(text = "INACTIVO", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
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
