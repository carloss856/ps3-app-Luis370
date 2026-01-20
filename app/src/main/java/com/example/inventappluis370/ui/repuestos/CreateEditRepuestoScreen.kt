package com.example.inventappluis370.ui.repuestos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.RepuestoRequest
import kotlinx.coroutines.launch

@Composable
fun CreateEditRepuestoScreen(
    navController: NavController,
    viewModel: RepuestosViewModel = hiltViewModel(),
    repuestoId: String? = null
) {
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var nivelCritico by remember { mutableStateOf("") }

    val isEditing = repuestoId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Cargar datos si estamos en modo edición
    LaunchedEffect(repuestoId) {
        if (isEditing) {
            viewModel.getRepuestoById(repuestoId!!)
        }
    }

    // Observar el repuesto seleccionado y rellenar los campos
    val selectedRepuesto by viewModel.selectedRepuesto.collectAsState()
    LaunchedEffect(selectedRepuesto) {
        selectedRepuesto?.let {
            nombre = it.nombreRepuesto ?: ""
            cantidad = (it.cantidadDisponible ?: 0).toString()
            nivelCritico = it.nivelCritico?.toString() ?: ""
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is RepuestosUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
        if (currentState is RepuestosUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(currentState.message)
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
            Text(text = if (isEditing) "Editar Repuesto" else "Nuevo Repuesto", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del Repuesto") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad Disponible") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = nivelCritico, onValueChange = { nivelCritico = it }, label = { Text("Nivel Crítico") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val repuestoRequest = RepuestoRequest(
                        nombreRepuesto = nombre,
                        cantidadDisponible = cantidad.toIntOrNull(),
                        nivelCritico = nivelCritico.toIntOrNull()
                    )
                    if (isEditing) {
                        viewModel.updateRepuesto(repuestoId!!, repuestoRequest)
                    } else {
                        viewModel.createRepuesto(repuestoRequest)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is RepuestosUiState.Loading
            ) {
                if(uiState is RepuestosUiState.Loading){
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}
