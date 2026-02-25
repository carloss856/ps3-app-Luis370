package com.example.inventappluis370.ui.tarifas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.TarifaServicioHistorial
import com.example.inventappluis370.ui.common.ModuleTopBar

@Composable
fun TarifaHistorialScreen(
    navController: NavController,
    tarifaId: String,
    viewModel: TarifaHistorialViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(tarifaId) {
        viewModel.loadHistorial(tarifaId)
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Historial de Tarifa",
                onBack = { navController.popBackStack() },
                onRefresh = { viewModel.loadHistorial(tarifaId) },
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is TarifaHistorialUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            is TarifaHistorialUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            is TarifaHistorialUiState.Success -> {
                if (state.items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        Text("Sin historial disponible.", modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    TarifaHistorialList(
                        items = state.items,
                        contentPadding = padding
                    )
                }
            }
        }
    }
}

@Composable
private fun TarifaHistorialList(
    items: List<TarifaServicioHistorial>,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${item.tipoTarea ?: "SIN TIPO"} - ${item.nivelTecnico ?: "N/A"}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = "${item.tarifaHora ?: 0.0} ${item.moneda ?: "USD"} / hora")
                    Text(text = "Fecha: ${item.fechaRegistro ?: "N/A"}")
                    Text(text = "Usuario: ${item.nombreUsuario ?: "N/A"} (${item.idUsuario ?: "N/A"})")
                }
            }
        }
    }
}

