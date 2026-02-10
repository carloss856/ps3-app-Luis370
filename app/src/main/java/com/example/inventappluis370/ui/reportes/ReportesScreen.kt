package com.example.inventappluis370.ui.reportes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
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
import com.example.inventappluis370.data.model.Reporte
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun ReportesScreen(
    navController: NavController,
    viewModel: ReportesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Escucha señal de refresco desde la pantalla de crear
    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getReportes()
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ReportesUiState.OperationSuccess) {
            viewModel.getReportes()
        }
    }

    val refreshing = uiState is ReportesUiState.Loading

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Reportes",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.BarChart,
                endIconContentDescription = "Reportes",
                onRefresh = { viewModel.getReportes() },
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("reportes/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Generar Reporte")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshContainer(
            refreshing = refreshing,
            onRefresh = { viewModel.getReportes() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ReportesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ReportesUiState.Error -> {
                    Text(
                        "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ReportesUiState.Success -> {
                    val reportes = state.reportes
                    if (reportes.isEmpty()) {
                        Text("No hay reportes para mostrar.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(reportes) { reporte ->
                                ReporteItem(reporte)
                            }
                        }
                    }
                }

                ReportesUiState.OperationSuccess -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun ReporteItem(reporte: Reporte) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = reporte.tipoReporte ?: "(Sin tipo)",
                style = MaterialTheme.typography.titleLarge
            )
            Text(text = "Generado por: Usuario #${reporte.idUsuario ?: "(desconocido)"}")
            Text(text = "Fecha: ${reporte.fechaGeneracion ?: "(sin fecha)"}")

            if (reporte.idReporte.isNullOrBlank()) {
                Text(
                    text = "ERROR: reporte sin id_reporte (debe corregirse en backend)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
