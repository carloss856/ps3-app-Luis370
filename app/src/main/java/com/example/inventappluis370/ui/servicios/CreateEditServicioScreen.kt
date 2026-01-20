package com.example.inventappluis370.ui.servicios

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.CreateServicioRequest
import kotlinx.coroutines.launch

@Composable
fun CreateEditServicioScreen(
    navController: NavController,
    viewModel: ServiciosViewModel = hiltViewModel(),
    servicioId: String? = null
) {
    var equipoId by remember { mutableStateOf("") }
    var problemaReportado by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var codigoRma by remember { mutableStateOf("") }
    var fechaIngreso by remember { mutableStateOf("") }

    val isEditing = servicioId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val fieldErrors by viewModel.fieldErrors.collectAsState()
    fun fieldError(key: String): String? = fieldErrors[key]?.firstOrNull()

    LaunchedEffect(servicioId) {
        if (isEditing) {
            viewModel.getServicioById(servicioId!!)
        }
    }

    val selectedServicio by viewModel.selectedServicio.collectAsState()
    LaunchedEffect(selectedServicio) {
        selectedServicio?.let {
            equipoId = it.idEquipo ?: ""
            problemaReportado = it.problemaReportado ?: ""
            estado = it.estado ?: ""
            codigoRma = it.codigoRma ?: ""
            fechaIngreso = it.fechaIngreso ?: ""
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is ServiciosUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
        if (uiState is ServiciosUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((uiState as ServiciosUiState.Error).message)
            }
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
            Text(text = if (isEditing) "Editar Servicio" else "Nuevo Servicio", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = equipoId,
                onValueChange = {
                    equipoId = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("ID del Equipo") },
                modifier = Modifier.fillMaxWidth(),
                isError = fieldError("id_equipo") != null,
                supportingText = { fieldError("id_equipo")?.let { Text(it) } }
            )

            OutlinedTextField(
                value = problemaReportado,
                onValueChange = {
                    problemaReportado = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Problema Reportado") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                isError = fieldError("problema_reportado") != null,
                supportingText = { fieldError("problema_reportado")?.let { Text(it) } }
            )

            OutlinedTextField(
                value = codigoRma,
                onValueChange = {
                    codigoRma = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Código RMA") },
                modifier = Modifier.fillMaxWidth(),
                isError = fieldError("codigo_rma") != null,
                supportingText = { fieldError("codigo_rma")?.let { Text(it) } }
            )

            OutlinedTextField(
                value = fechaIngreso,
                onValueChange = {
                    fechaIngreso = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Fecha Ingreso (ISO8601)") },
                modifier = Modifier.fillMaxWidth(),
                isError = fieldError("fecha_ingreso") != null,
                supportingText = { fieldError("fecha_ingreso")?.let { Text(it) } }
            )

            OutlinedTextField(
                value = estado,
                onValueChange = {
                    estado = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Estado (Pendiente, En proceso, Finalizado)") },
                modifier = Modifier.fillMaxWidth(),
                isError = fieldError("estado") != null,
                supportingText = { fieldError("estado")?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val request = CreateServicioRequest(
                        idEquipo = equipoId,
                        codigoRma = codigoRma,
                        fechaIngreso = fechaIngreso,
                        problemaReportado = problemaReportado,
                        estado = estado
                    )
                    if (isEditing) {
                        viewModel.updateServicio(servicioId!!, request)
                    } else {
                        viewModel.createServicio(request)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is ServiciosUiState.Loading
            ) {
                if (uiState is ServiciosUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}
