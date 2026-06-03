package com.example.inventappluis370.ui.repuestos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.RepuestoRequest
import com.example.inventappluis370.ui.common.ModuleTopBar
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction

@Composable
fun CreateEditRepuestoScreen(
    navController: NavController,
    viewModel: RepuestosViewModel = hiltViewModel(),
    repuestoId: String? = null
) {
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("0") }
    var nivelCritico by remember { mutableStateOf("") }
    var loadedFromSelected by remember { mutableStateOf(false) }

    val isEditing = repuestoId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Cargar datos si estamos en modo edicion
    LaunchedEffect(repuestoId) {
        if (isEditing) {
            viewModel.getRepuestoById(repuestoId!!)
        }
    }

    // Observar el repuesto seleccionado y rellenar los campos
    val selectedRepuesto by viewModel.selectedRepuesto.collectAsState()
    LaunchedEffect(selectedRepuesto, isEditing, loadedFromSelected) {
        selectedRepuesto?.let {
            if (!isEditing || loadedFromSelected) return@let
            nombre = it.nombreRepuesto ?: ""
            cantidad = (it.cantidadDisponible ?: 0).toString()
            nivelCritico = it.nivelCritico?.toString() ?: ""
            loadedFromSelected = true
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is RepuestosUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.previousBackStackEntry?.savedStateHandle?.set(
                "operation_message",
                if (isEditing) "Repuesto editado correctamente" else "Repuesto creado correctamente"
            )
            navController.popBackStack()
        }
        if (currentState is RepuestosUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(currentState.message)
            }
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = if (isEditing) "Editar Repuesto" else "Nuevo Repuesto",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Construction,
                endIconContentDescription = "Repuestos",
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Repuesto *") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cantidad,
                onValueChange = {},
                readOnly = true,
                label = { Text("Cantidad Disponible *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = nivelCritico,
                onValueChange = { nivelCritico = it },
                label = { Text("Nivel Critico *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) { Text("Volver") }

                Button(
                    onClick = {
                        val cantidadInt = cantidad.toIntOrNull()
                        val nivelCriticoInt = nivelCritico.toIntOrNull()
                        if (nombre.isBlank() || cantidadInt == null || nivelCriticoInt == null) {
                            scope.launch { snackbarHostState.showSnackbar("Completa los campos obligatorios") }
                            return@Button
                        }

                        val repuestoRequest = RepuestoRequest(
                            nombreRepuesto = nombre.trim(),
                            cantidadDisponible = cantidadInt.coerceAtLeast(0),
                            nivelCritico = nivelCriticoInt.coerceAtLeast(0)
                        )
                        if (isEditing) {
                            viewModel.updateRepuesto(repuestoId!!, repuestoRequest)
                        } else {
                            viewModel.createRepuesto(repuestoRequest)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is RepuestosUiState.Loading
                ) {
                    if (uiState is RepuestosUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (isEditing) "Actualizar" else "Guardar")
                    }
                }
            }
        }
    }
}



