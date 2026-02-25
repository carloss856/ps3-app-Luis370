package com.example.inventappluis370.ui.inventario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.Repuesto
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.repuestos.RepuestosUiState
import com.example.inventappluis370.ui.repuestos.RepuestosViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInventarioEntradaScreen(
    navController: NavController,
    viewModel: InventarioViewModel = hiltViewModel(),
    repuestosViewModel: RepuestosViewModel = hiltViewModel(),
) {
    var repuestoId by remember { mutableStateOf("") }
    var repuestoLabel by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }

    var repuestoExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val inventarioState by viewModel.uiState.collectAsState()
    LaunchedEffect(inventarioState) {
        val currentState = inventarioState
        if (currentState is InventarioUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.previousBackStackEntry?.savedStateHandle?.set("operation_message", "Entrada creada correctamente")
            navController.popBackStack()
        }
        if (currentState is InventarioUiState.Error) {
            scope.launch { snackbarHostState.showSnackbar(currentState.message) }
        }
    }

    val repuestosState by repuestosViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        if (repuestosState !is RepuestosUiState.Success) {
            repuestosViewModel.getRepuestos()
        }
    }

    val repuestos: List<Repuesto> = (repuestosState as? RepuestosUiState.Success)?.repuestos.orEmpty()

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Nueva Entrada de Inventario",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Inventory,
                endIconContentDescription = "Inventario",
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
            Text(
                "Selecciona el repuesto y registra la entrada.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )

            ExposedDropdownMenuBox(
                expanded = repuestoExpanded,
                onExpandedChange = { repuestoExpanded = !repuestoExpanded },
            ) {
                OutlinedTextField(
                    value = repuestoLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Repuesto *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        if (repuestosState is RepuestosUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = repuestoExpanded)
                        }
                    },
                    singleLine = true,
                )

                ExposedDropdownMenu(
                    expanded = repuestoExpanded,
                    onDismissRequest = { repuestoExpanded = false },
                ) {
                    if (repuestos.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                val msg = when (repuestosState) {
                                    is RepuestosUiState.Loading -> "Cargando repuestos..."
                                    is RepuestosUiState.Error -> "No se pudieron cargar repuestos"
                                    else -> "Sin repuestos"
                                }
                                Text(msg)
                            },
                            onClick = { repuestoExpanded = false },
                            enabled = false,
                        )
                    } else {
                        repuestos.forEach { r ->
                            val id = r.idRepuesto.orEmpty()
                            val nombre = r.nombreRepuesto ?: "(Sin nombre)"
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = {
                                    repuestoId = id
                                    repuestoLabel = nombre
                                    repuestoExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad de entrada") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val cantidadInt = cantidad.trim().toIntOrNull()
                    when {
                        repuestoId.isBlank() -> scope.launch {
                            snackbarHostState.showSnackbar("Debes seleccionar un repuesto")
                        }
                        cantidadInt == null -> scope.launch {
                            snackbarHostState.showSnackbar("Cantidad invalida: usa solo numeros enteros")
                        }
                        cantidadInt <= 0 -> scope.launch {
                            snackbarHostState.showSnackbar("La cantidad debe ser mayor que cero")
                        }
                        else -> viewModel.createInventarioEntrada(repuestoId.trim(), cantidadInt)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = inventarioState !is InventarioUiState.Loading
            ) {
                if (inventarioState is InventarioUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Entrada")
                }
            }
        }
    }
}



