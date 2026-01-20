package com.example.inventappluis370.ui.repuestos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.Repuesto
import com.example.inventappluis370.ui.common.ModuleTopBar

@Composable
fun RepuestosScreen(
    navController: NavController,
    viewModel: RepuestosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getRepuestos()
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Repuestos",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Settings,
                endIconContentDescription = "Repuestos"
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("repuestos/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Repuesto")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = uiState) {
                        is RepuestosUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        is RepuestosUiState.Error -> {
                            Text(
                                "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        is RepuestosUiState.Success -> {
                            val repuestos = state.repuestos
                            if (repuestos.isEmpty()) {
                                Text(text = "No hay repuestos para mostrar.", modifier = Modifier.align(Alignment.Center))
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(repuestos) { repuesto ->
                                        val id = repuesto.idRepuesto
                                        RepuestoItem(
                                            repuesto = repuesto,
                                            onDelete = { if (!id.isNullOrBlank()) viewModel.deleteRepuesto(id) },
                                            onEdit = { if (!id.isNullOrBlank()) navController.navigate("repuestos/$id") },
                                            canUpdate = viewModel.canUpdate(),
                                            canDelete = viewModel.canDelete()
                                        )
                                    }
                                }
                            }
                        }

                        RepuestosUiState.OperationSuccess -> {
                            LaunchedEffect(Unit) { viewModel.getRepuestos() }
                        }
                    }
                }
            }
        }
    }
}

@Composable fun RepuestoItem(
    repuesto: Repuesto,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    val id = repuesto.idRepuesto

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(repuesto.nombreRepuesto ?: "(Sin nombre)", fontWeight = FontWeight.Bold)
                Text("Cantidad: ${repuesto.cantidadDisponible?.toString() ?: "(sin dato)"}")

                if (!id.isNullOrBlank()) {
                    Text("ID: $id", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        "ERROR: repuesto sin id_repuesto (debe corregirse en backend)",
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
