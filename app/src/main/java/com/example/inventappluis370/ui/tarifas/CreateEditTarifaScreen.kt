package com.example.inventappluis370.ui.tarifas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.CreateTarifaRequest
import com.example.inventappluis370.data.model.UpdateTarifaRequest
import com.example.inventappluis370.ui.common.ModuleTopBar
import kotlinx.coroutines.launch

@Composable
fun CreateEditTarifaScreen(
    navController: NavController,
    viewModel: TarifasViewModel = hiltViewModel(),
    tarifaId: String? = null
) {
    var tipoTarea by remember { mutableStateOf("") }
    var nivelTecnico by remember { mutableStateOf("") }
    var tarifaHora by remember { mutableStateOf("") }
    var moneda by remember { mutableStateOf("USD") }
    var activo by remember { mutableStateOf(true) }
    var vigenteDesde by remember { mutableStateOf("") }

    val isEditing = tarifaId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val fieldErrors by viewModel.fieldErrors.collectAsState()
    fun fieldError(key: String): String? = fieldErrors[key]?.firstOrNull()

    LaunchedEffect(tarifaId) {
        if (isEditing) {
            viewModel.getTarifaById(tarifaId!!)
        }
    }

    val selectedTarifa by viewModel.selectedTarifa.collectAsState()
    LaunchedEffect(selectedTarifa) {
        selectedTarifa?.let {
            tipoTarea = it.tipoTarea ?: ""
            nivelTecnico = it.nivelTecnico ?: ""
            tarifaHora = (it.tarifaHora ?: 0.0).toString()
            moneda = it.moneda ?: "USD"
            activo = it.activo ?: true
            vigenteDesde = it.vigenteDesde ?: ""
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is TarifasUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
        if (uiState is TarifasUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((uiState as TarifasUiState.Error).message)
            }
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = if (isEditing) "Editar Tarifa" else "Nueva Tarifa",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.AttachMoney,
                endIconContentDescription = "Tarifas",
                onRefresh = if (isEditing) {
                    {
                        tarifaId?.let { viewModel.getTarifaById(it) }
                    }
                } else null,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isEditing) {
                OutlinedTextField(
                    value = tipoTarea,
                    onValueChange = {
                        tipoTarea = it
                        viewModel.clearFieldErrors()
                    },
                    label = { Text("Tipo de Tarea (software, físico, etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = fieldError("tipo_tarea") != null,
                    supportingText = { fieldError("tipo_tarea")?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = nivelTecnico,
                    onValueChange = {
                        nivelTecnico = it
                        viewModel.clearFieldErrors()
                    },
                    label = { Text("Nivel Técnico (junior, senior, etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = fieldError("nivel_tecnico") != null,
                    supportingText = { fieldError("nivel_tecnico")?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = vigenteDesde,
                    onValueChange = {
                        vigenteDesde = it
                        viewModel.clearFieldErrors()
                    },
                    label = { Text("Vigente Desde (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = fieldError("vigente_desde") != null,
                    supportingText = { fieldError("vigente_desde")?.let { Text(it) } }
                )
            }

            OutlinedTextField(
                value = tarifaHora,
                onValueChange = {
                    tarifaHora = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Tarifa por Hora") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = fieldError("tarifa_hora") != null,
                supportingText = { fieldError("tarifa_hora")?.let { Text(it) } }
            )

            OutlinedTextField(
                value = moneda,
                onValueChange = {
                    moneda = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Moneda") },
                modifier = Modifier.fillMaxWidth(),
                isError = fieldError("moneda") != null,
                supportingText = { fieldError("moneda")?.let { Text(it) } }
            )

            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Tarifa Activa", modifier = Modifier.weight(1f))
                Switch(checked = activo, onCheckedChange = {
                    activo = it
                    viewModel.clearFieldErrors()
                })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val tarifaVal = tarifaHora.toDoubleOrNull() ?: 0.0
                    val nivelTecnicoNullable = nivelTecnico.trim().ifBlank { null }
                    if (isEditing) {
                        viewModel.updateTarifa(tarifaId!!, UpdateTarifaRequest(tarifaVal, moneda, activo))
                    } else {
                        viewModel.createTarifa(
                            CreateTarifaRequest(
                                tipoTarea = tipoTarea,
                                nivelTecnico = nivelTecnicoNullable,
                                tarifaHora = tarifaVal,
                                moneda = moneda,
                                activo = activo,
                                vigenteDesde = vigenteDesde
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is TarifasUiState.Loading
            ) {
                if (uiState is TarifasUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Tarifa")
                }
            }
        }
    }
}
