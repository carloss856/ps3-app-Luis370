package com.example.inventappluis370.ui.equipos

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
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
import com.example.inventappluis370.data.model.Equipo
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun EquiposScreen(
    navController: NavController,
    viewModel: EquiposViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val refreshing = uiState is EquiposUiState.Loading

    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getEquipos()
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Equipos",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Devices,
                endIconContentDescription = "Equipos"
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("equipos/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Equipo")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            PullToRefreshContainer(
                refreshing = refreshing,
                onRefresh = { viewModel.getEquipos() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is EquiposUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is EquiposUiState.Error -> {
                        Text(
                            "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is EquiposUiState.Success -> {
                        val equipos = state.equipos
                        if (equipos.isEmpty()) {
                            Text("No hay equipos para mostrar.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(equipos) { equipo ->
                                    val id = equipo.idEquipo
                                    EquipoItem(
                                        equipo = equipo,
                                        onDelete = { if (!id.isNullOrBlank()) viewModel.deleteEquipo(id) },
                                        onEdit = { if (!id.isNullOrBlank()) navController.navigate("equipos/$id") },
                                        canUpdate = viewModel.canUpdate(),
                                        canDelete = viewModel.canDelete()
                                    )
                                }
                            }
                        }
                    }

                    EquiposUiState.OperationSuccess -> {
                        LaunchedEffect(Unit) { viewModel.getEquipos() }
                    }
                }
            }
        }
    }
}

@Composable
fun EquipoItem(
    equipo: Equipo,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    val id = equipo.idEquipo

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(equipo.tipoEquipo ?: "(Sin tipo)", style = MaterialTheme.typography.titleMedium)

                val marcaModelo = listOfNotNull(equipo.marca, equipo.modelo).joinToString(" ")
                if (marcaModelo.isNotBlank()) Text(marcaModelo)

                val duenio = equipo.propiedad?.idPersona
                if (!duenio.isNullOrBlank()) {
                    Text("Dueño: $duenio", style = MaterialTheme.typography.bodySmall)
                }

                if (!id.isNullOrBlank()) {
                    Text("ID: $id", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        "ERROR: equipo sin id_equipo (debe corregirse en backend)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
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
