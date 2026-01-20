package com.example.inventappluis370.ui.equipos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.EquipoRequest
import kotlinx.coroutines.launch

@Composable
fun CreateEditEquipoScreen(
    navController: NavController,
    viewModel: EquiposViewModel = hiltViewModel(),
    equipoId: String? = null
) {
    var tipoEquipo by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var idAsignado by remember { mutableStateOf("") }

    val isEditing = equipoId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(equipoId) {
        if (isEditing) {
            viewModel.getEquipoById(equipoId!!)
        }
    }

    val selectedEquipo by viewModel.selectedEquipo.collectAsState()
    LaunchedEffect(selectedEquipo) {
        selectedEquipo?.let {
            tipoEquipo = it.tipoEquipo ?: ""
            marca = it.marca ?: ""
            modelo = it.modelo ?: ""
            idAsignado = it.idAsignado ?: ""
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is EquiposUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
        if (currentState is EquiposUiState.Error) {
            scope.launch { snackbarHostState.showSnackbar(currentState.message) }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isEditing) "Editar Equipo" else "Nuevo Equipo",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = tipoEquipo,
                onValueChange = { tipoEquipo = it },
                label = { Text("Tipo de Equipo *") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = marca,
                onValueChange = { marca = it },
                label = { Text("Marca") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = modelo,
                onValueChange = { modelo = it },
                label = { Text("Modelo") },
                modifier = Modifier.fillMaxWidth()
            )

            // Según contrato/controller: el backend recibe id_asignado (id_persona del usuario asignado)
            OutlinedTextField(
                value = idAsignado,
                onValueChange = { idAsignado = it },
                label = { Text("ID Asignado (id_persona del usuario)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (tipoEquipo.isBlank()) {
                            snackbarHostState.showSnackbar("Tipo de equipo es requerido")
                            return@launch
                        }

                        // IMPORTANTE:
                        // - En BD: equipos tiene id_persona (puede venir null), pero el endpoint store valida id_asignado.
                        // - PropiedadEquipo se crea en el backend, no debemos enviar propiedad.id_persona desde aquí.
                        val equipoRequest = EquipoRequest(
                            tipoEquipo = tipoEquipo,
                            marca = marca.ifBlank { null },
                            modelo = modelo.ifBlank { null },
                            idPersona = null,
                            idAsignado = idAsignado.ifBlank { null }
                        )

                        if (isEditing) {
                            viewModel.updateEquipo(equipoId!!, equipoRequest)
                        } else {
                            viewModel.createEquipo(equipoRequest)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is EquiposUiState.Loading
            ) {
                if (uiState is EquiposUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}
