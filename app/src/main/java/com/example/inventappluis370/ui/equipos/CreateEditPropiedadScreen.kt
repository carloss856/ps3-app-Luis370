package com.example.inventappluis370.ui.equipos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.PropiedadEquipoRequest
import kotlinx.coroutines.launch

@Composable
fun CreateEditPropiedadScreen(
    navController: NavController,
    viewModel: PropiedadEquipoViewModel = hiltViewModel(),
    propiedadId: String? = null
) {
    var equipoId by remember { mutableStateOf("") }
    var personaId by remember { mutableStateOf("") }

    val isEditing = propiedadId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(propiedadId) {
        if (isEditing) {
            viewModel.getPropiedadById(propiedadId!!)
        }
    }

    val selected by viewModel.selectedPropiedad.collectAsState()
    LaunchedEffect(selected) {
        selected?.let {
            // idEquipo/idPersona pueden venir null en datos reales
            equipoId = it.idEquipo ?: ""
            personaId = it.idPersona ?: ""
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is PropiedadEquipoUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
        if (uiState is PropiedadEquipoUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((uiState as PropiedadEquipoUiState.Error).message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isEditing) "Editar Propiedad" else "Nueva Asignación",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = equipoId,
                onValueChange = { equipoId = it },
                label = { Text("ID del Equipo (id_equipo)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = personaId,
                onValueChange = { personaId = it },
                label = { Text("ID de la Persona (id_persona)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (equipoId.isBlank() || personaId.isBlank()) {
                            snackbarHostState.showSnackbar("id_equipo e id_persona son requeridos")
                            return@launch
                        }

                        val request = PropiedadEquipoRequest(
                            idEquipo = equipoId.trim(),
                            idPersona = personaId.trim()
                        )
                        if (isEditing) {
                            viewModel.updatePropiedad(propiedadId!!, request)
                        } else {
                            viewModel.createPropiedad(request)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is PropiedadEquipoUiState.Loading
            ) {
                if (uiState is PropiedadEquipoUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}
