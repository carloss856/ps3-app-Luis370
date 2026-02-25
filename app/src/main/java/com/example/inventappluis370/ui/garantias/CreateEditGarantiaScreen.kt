package com.example.inventappluis370.ui.garantias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.GarantiaRequest
import com.example.inventappluis370.data.model.Servicio
import com.example.inventappluis370.ui.common.DatePickerField
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.servicios.ServiciosUiState
import com.example.inventappluis370.ui.servicios.ServiciosViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditGarantiaScreen(
    navController: NavController,
    viewModel: GarantiasViewModel = hiltViewModel(),
    serviciosViewModel: ServiciosViewModel = hiltViewModel(),
    garantiaId: String? = null
) {
    var servicioId by remember { mutableStateOf("") }
    var servicioLabel by remember { mutableStateOf("") }
    var servicioExpanded by remember { mutableStateOf(false) }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    val isEditing = garantiaId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(garantiaId) {
        if (isEditing) {
            viewModel.getGarantiaById(garantiaId!!)
        }
    }

    val serviciosState by serviciosViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        if (serviciosState !is ServiciosUiState.Success) serviciosViewModel.getServicios()
    }
    val servicios = (serviciosState as? ServiciosUiState.Success)?.servicios.orEmpty()

    val selected by viewModel.selectedGarantia.collectAsState()
    LaunchedEffect(selected, servicios) {
        selected?.let {
            servicioId = it.servicioId ?: ""
            fechaInicio = it.fechaInicio ?: ""
            fechaFin = it.fechaFin ?: ""
            observaciones = it.observaciones ?: ""
            servicioLabel = servicios.firstOrNull { s -> s.idServicio == servicioId }?.let { s ->
                s.problemaReportado?.takeIf { txt -> txt.isNotBlank() } ?: (s.estado ?: servicioId)
            } ?: servicioId
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is GarantiasUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.previousBackStackEntry?.savedStateHandle?.set(
                "operation_message",
                if (isEditing) "Garantia editada correctamente" else "Garantia creada correctamente"
            )
            navController.popBackStack()
        }
        if (uiState is GarantiasUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((uiState as GarantiasUiState.Error).message)
            }
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = if (isEditing) "Editar Garantia" else "Nueva Garantia",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Verified,
                endIconContentDescription = "Garantias"
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = servicioExpanded,
                onExpandedChange = { servicioExpanded = !servicioExpanded }
            ) {
                OutlinedTextField(
                    value = servicioLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Servicio *") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = servicioExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = servicioExpanded,
                    onDismissRequest = { servicioExpanded = false }
                ) {
                    servicios.forEach { s: Servicio ->
                        val id = s.idServicio.orEmpty()
                        val label = s.problemaReportado?.takeIf { it.isNotBlank() } ?: (s.estado ?: id)
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                servicioId = id
                                servicioLabel = label
                                servicioExpanded = false
                            }
                        )
                    }
                }
            }

            DatePickerField(
                label = "Fecha inicio",
                value = fechaInicio,
                onDateSelected = { fechaInicio = it }
            )

            DatePickerField(
                label = "Fecha fin",
                value = fechaFin,
                onDateSelected = { fechaFin = it }
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val request = GarantiaRequest(servicioId, fechaInicio, fechaFin, observaciones)
                    if (isEditing) {
                        viewModel.updateGarantia(garantiaId!!, request)
                    } else {
                        viewModel.createGarantia(request)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is GarantiasUiState.Loading
            ) {
                if (uiState is GarantiasUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}



