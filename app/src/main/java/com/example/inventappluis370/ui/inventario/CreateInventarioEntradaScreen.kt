package com.example.inventappluis370.ui.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var cantidad by remember { mutableStateOf("") }

    // Dropdown estado
    var repuestoExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val inventarioState by viewModel.uiState.collectAsState()
    LaunchedEffect(inventarioState) {
        val currentState = inventarioState
        if (currentState is InventarioUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
        if (currentState is InventarioUiState.Error) {
            scope.launch { snackbarHostState.showSnackbar(currentState.message) }
        }
    }

    // Cargar catálogo de repuestos para dropdown
    val repuestosState by repuestosViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        // Evita recargar si ya hay data.
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Selecciona el repuesto y registra la entrada. Si el catálogo no carga, puedes escribir el ID manualmente.",
                style = MaterialTheme.typography.bodyMedium
            )

            // Dropdown de repuestos (id + nombre)
            ExposedDropdownMenuBox(
                expanded = repuestoExpanded,
                onExpandedChange = { repuestoExpanded = !repuestoExpanded },
            ) {
                OutlinedTextField(
                    value = repuestoId,
                    onValueChange = { repuestoId = it },
                    label = { Text("ID del Repuesto") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        if (repuestosState is RepuestosUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
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
                                    is RepuestosUiState.Loading -> "Cargando repuestos…"
                                    is RepuestosUiState.Error -> "No se pudieron cargar repuestos"
                                    else -> "Sin repuestos"
                                }
                                Text(msg)
                            },
                            onClick = { repuestoExpanded = false },
                            enabled = false,
                        )
                    } else {
                        // Usamos LazyColumn de menú para evitar problemas de rendimiento con listas largas.
                        repuestos.forEach { r ->
                            val id = r.idRepuesto.orEmpty()
                            val nombre = r.nombreRepuesto ?: "(Sin nombre)"
                            DropdownMenuItem(
                                text = { Text("$id — $nombre") },
                                onClick = {
                                    repuestoId = id
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
                label = { Text("Cantidad de Entrada") },
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
