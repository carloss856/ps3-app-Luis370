package com.example.inventappluis370.ui.servicios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.CreateParteTrabajoRequest
import com.example.inventappluis370.data.model.UpdateParteTrabajoRequest
import kotlinx.coroutines.launch

@Composable
fun CreateEditParteTrabajoScreen(
    navController: NavController,
    servicioId: String,
    viewModel: PartesTrabajoViewModel = hiltViewModel(),
    parteId: String? = null
) {
    var tipoTarea by remember { mutableStateOf("") }
    var minutos by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    val isEditing = parteId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val fieldErrors by viewModel.fieldErrors.collectAsState()
    fun fieldError(key: String): String? = fieldErrors[key]?.firstOrNull()

    LaunchedEffect(parteId) {
        if (isEditing) {
            viewModel.getParteById(servicioId, parteId!!)
        }
    }

    val selectedParte by viewModel.selectedParte.collectAsState()
    LaunchedEffect(selectedParte) {
        selectedParte?.let { parte ->
            tipoTarea = parte.tipoTarea ?: ""
            minutos = parte.minutos.toString()
            notas = parte.notas ?: ""
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is PartesTrabajoUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
        if (uiState is PartesTrabajoUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((uiState as PartesTrabajoUiState.Error).message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = if (isEditing) "Editar Parte de Trabajo" else "Nuevo Parte de Trabajo", style = MaterialTheme.typography.headlineSmall)

            if (!isEditing) {
                OutlinedTextField(
                    value = tipoTarea,
                    onValueChange = { newValue ->
                        tipoTarea = newValue
                        viewModel.clearFieldErrors()
                    },
                    label = { Text("Tipo de Tarea") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = fieldError("tipo_tarea") != null,
                    supportingText = { fieldError("tipo_tarea")?.let { err -> Text(err) } }
                )
            }

            OutlinedTextField(
                value = minutos,
                onValueChange = { newValue ->
                    minutos = newValue
                    viewModel.clearFieldErrors()
                },
                label = { Text("Minutos Trabajados") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = fieldError("minutos") != null,
                supportingText = { fieldError("minutos")?.let { err -> Text(err) } }
            )

            OutlinedTextField(
                value = notas,
                onValueChange = { newValue ->
                    notas = newValue
                    viewModel.clearFieldErrors()
                },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                isError = fieldError("notas") != null,
                supportingText = { fieldError("notas")?.let { err -> Text(err) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val minInt = minutos.toIntOrNull() ?: 0
                    if (isEditing) {
                        viewModel.updateParte(servicioId, parteId!!, UpdateParteTrabajoRequest(minInt, notas))
                    } else {
                        viewModel.createParte(servicioId, CreateParteTrabajoRequest(tipoTarea, minInt, notas))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is PartesTrabajoUiState.Loading
            ) {
                if (uiState is PartesTrabajoUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Parte")
                }
            }
        }
    }
}
