package com.example.inventappluis370.ui.garantias

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.GarantiaRequest
import kotlinx.coroutines.launch

@Composable
fun EditGarantiaScreen(
    navController: NavController,
    viewModel: GarantiasViewModel = hiltViewModel(),
    garantiaId: String
) {
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(garantiaId) {
        viewModel.getGarantiaById(garantiaId)
    }

    val selectedGarantia by viewModel.selectedGarantia.collectAsState()
    LaunchedEffect(selectedGarantia) {
        selectedGarantia?.let {
            fechaInicio = it.fechaInicio ?: ""
            fechaFin = it.fechaFin ?: ""
            observaciones = it.observaciones ?: ""
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is GarantiasUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
        if (uiState is GarantiasUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((uiState as GarantiasUiState.Error).message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Editar Garantía", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(value = fechaInicio, onValueChange = { fechaInicio = it }, label = { Text("Fecha de Inicio (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = fechaFin, onValueChange = { fechaFin = it }, label = { Text("Fecha de Fin (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val servicioId = selectedGarantia?.servicioId
                    if (servicioId.isNullOrBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("La garantía no tiene id_servicio (dato inválido)") }
                        return@Button
                    }
                    val request = GarantiaRequest(
                        servicioId = servicioId,
                        fechaInicio = fechaInicio,
                        fechaFin = fechaFin,
                        observaciones = observaciones
                    )
                    viewModel.updateGarantia(garantiaId, request)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is GarantiasUiState.Loading
            ) {
                 if (uiState is GarantiasUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Cambios")
                }
            }
        }
    }
}
