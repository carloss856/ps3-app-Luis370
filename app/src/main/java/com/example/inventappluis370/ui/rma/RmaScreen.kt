package com.example.inventappluis370.ui.rma

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.inventappluis370.data.model.RMA
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun RmaScreen(
    viewModel: RmaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val refreshing = uiState is RmaUiState.Loading

    Scaffold(
        topBar = { ModuleTopBar(title = "Listado de RMA") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PullToRefreshContainer(
                refreshing = refreshing,
                onRefresh = { viewModel.getRmas() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val state = uiState) {
                    is RmaUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is RmaUiState.Error -> {
                        Text(
                            "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is RmaUiState.Success -> {
                        val rmas = state.rmas
                        if (rmas.isEmpty()) {
                            Text("No hay RMAs registrados.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(rmas) { rma ->
                                    val id = rma.rma
                                    RmaItem(
                                        rma = rma,
                                        canDelete = viewModel.canDelete() && !id.isNullOrBlank(),
                                        onDelete = { if (!id.isNullOrBlank()) viewModel.deleteRma(id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RmaItem(rma: RMA, canDelete: Boolean, onDelete: () -> Unit) {
    val id = rma.rma

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                if (!id.isNullOrBlank()) {
                    Text("RMA: $id", style = MaterialTheme.typography.titleMedium)
                } else {
                    Text(
                        "ERROR: registro RMA sin campo rma (debe corregirse en backend)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    "Usuario: ${rma.idPersona ?: "(desconocido)"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Fecha: ${rma.fechaCreacion ?: "(sin fecha)"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (canDelete && !id.isNullOrBlank()) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}
