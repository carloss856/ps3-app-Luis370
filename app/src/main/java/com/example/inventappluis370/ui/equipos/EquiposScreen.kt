package com.example.inventappluis370.ui.equipos

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
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.Equipo
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer
import com.example.inventappluis370.ui.usuarios.UsuariosUiState
import com.example.inventappluis370.ui.usuarios.UsuariosViewModel

@Composable
fun EquiposScreen(
    navController: NavController,
    viewModel: EquiposViewModel = hiltViewModel(),
    usuariosViewModel: UsuariosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val usuariosState by usuariosViewModel.uiState.collectAsState()

    val userNameById = (usuariosState as? UsuariosUiState.Success)
        ?.users
        ?.associate { (it.idPersona ?: it.idRaw ?: "") to (it.nombre ?: (it.idPersona ?: it.idRaw ?: "")) }
        .orEmpty()

    var selectedEquipo by remember { mutableStateOf<Equipo?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var operationMessage by remember { mutableStateOf<String?>(null) }

    val refreshing = uiState is EquiposUiState.Loading

    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getEquipos()
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }

    LaunchedEffect(Unit) {
        usuariosViewModel.getUsers()
        val msg = navController.currentBackStackEntry?.savedStateHandle?.get<String>("operation_message")
        if (!msg.isNullOrBlank()) {
            operationMessage = msg
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("operation_message")
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
                    Icon(Icons.Default.Add, contentDescription = "Anadir Equipo")
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
                                    val ownerId = equipo.idAsignado ?: equipo.propiedad?.idPersona.orEmpty()
                                    EquipoItem(
                                        equipo = equipo,
                                        ownerName = userNameById[ownerId],
                                        onDelete = { if (!id.isNullOrBlank()) pendingDeleteId = id },
                                        onEdit = { if (!id.isNullOrBlank()) navController.navigate("equipos/$id") },
                                        onView = { selectedEquipo = equipo },
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

        if (selectedEquipo != null) {
            val e = selectedEquipo!!
            val ownerId = e.idAsignado ?: e.propiedad?.idPersona.orEmpty()
            AlertDialog(
                onDismissRequest = { selectedEquipo = null },
                confirmButton = { TextButton(onClick = { selectedEquipo = null }) { Text("Cerrar") } },
                title = { Text("Detalle de equipo") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Tipo: ${e.tipoEquipo ?: "-"}")
                        Text("Marca: ${e.marca ?: "-"}")
                        Text("Modelo: ${e.modelo ?: "-"}")
                        Text("Dueno: ${userNameById[ownerId] ?: ownerId.ifBlank { "-" }}")
                    }
                }
            )
        }

        if (pendingDeleteId != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteId = null },
                confirmButton = {
                    TextButton(onClick = {
                        val id = pendingDeleteId
                        pendingDeleteId = null
                        if (!id.isNullOrBlank()) {
                            viewModel.deleteEquipo(id)
                            operationMessage = "Equipo eliminado correctamente"
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text("Cancelar") } },
                title = { Text("Confirmar eliminacion") },
                text = { Text("Deseas eliminar este equipo?") }
            )
        }

        if (operationMessage != null) {
            AlertDialog(
                onDismissRequest = { operationMessage = null },
                confirmButton = { TextButton(onClick = { operationMessage = null }) { Text("Aceptar") } },
                title = { Text("Operacion completada") },
                text = { Text(operationMessage!!) }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EquipoItem(
    equipo: Equipo,
    ownerName: String?,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    val id = equipo.idEquipo

    Card(modifier = Modifier.fillMaxWidth(), onClick = onView) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(equipo.tipoEquipo ?: "(Sin tipo)", style = MaterialTheme.typography.titleMedium)

                val marcaModelo = listOfNotNull(equipo.marca, equipo.modelo).joinToString(" ")
                if (marcaModelo.isNotBlank()) Text(marcaModelo)

                val duenio = ownerName ?: equipo.propiedad?.idPersona
                if (!duenio.isNullOrBlank()) {
                    Text("Dueno: $duenio", style = MaterialTheme.typography.bodySmall)
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

