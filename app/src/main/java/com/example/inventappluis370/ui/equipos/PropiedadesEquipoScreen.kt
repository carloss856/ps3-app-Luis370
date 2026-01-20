package com.example.inventappluis370.ui.equipos

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
import com.example.inventappluis370.data.model.PropiedadEquipo
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun PropiedadesEquipoScreen(
    navController: NavController,
    viewModel: PropiedadEquipoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val refreshing = uiState is PropiedadEquipoUiState.Loading

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
                        text = "Propiedades de Equipos",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("propiedad-equipos/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Asignar Propiedad")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshContainer(
            refreshing = refreshing,
            onRefresh = { viewModel.getPropiedades() },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PropiedadEquipoUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is PropiedadEquipoUiState.Error -> {
                    Text(
                        "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is PropiedadEquipoUiState.Success -> {
                    val propiedades = state.propiedades
                    if (propiedades.isEmpty()) {
                        Text("No hay asignaciones registradas.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        // TEMP: UI mínima para estabilizar compilación.
                        Text(
                            text = "Asignaciones cargadas: ${propiedades.size}",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                PropiedadEquipoUiState.OperationSuccess -> {
                    LaunchedEffect(Unit) { viewModel.getPropiedades() }
                }
            }
        }
    }
}

@Composable
fun PropiedadItem(propiedad: PropiedadEquipo, onDelete: () -> Unit, onEdit: () -> Unit, canUpdate: Boolean, canDelete: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Equipo: ${propiedad.idEquipo}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Dueño: ${propiedad.idPersona}")
                Text(text = "ID Propiedad: ${propiedad.idPropiedad}", style = MaterialTheme.typography.bodySmall)
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
