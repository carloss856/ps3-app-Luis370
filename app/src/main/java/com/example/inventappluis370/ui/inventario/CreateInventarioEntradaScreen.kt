package com.example.inventappluis370.ui.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun CreateInventarioEntradaScreen(
    navController: NavController,
    viewModel: InventarioViewModel = hiltViewModel()
) {
    var repuestoId by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is InventarioUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
        if (currentState is InventarioUiState.Error) {
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
            Text("Nueva Entrada de Inventario", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(value = repuestoId, onValueChange = { repuestoId = it }, label = { Text("ID del Repuesto") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad de Entrada") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val cantidadInt = cantidad.toIntOrNull() ?: 0
                    if (repuestoId.isNotBlank() && cantidadInt > 0) {
                        viewModel.createInventarioEntrada(repuestoId, cantidadInt)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is InventarioUiState.Loading
            ) {
                if (uiState is InventarioUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Entrada")
                }
            }
        }
    }
}
