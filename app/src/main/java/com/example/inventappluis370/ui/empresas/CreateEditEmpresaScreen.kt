package com.example.inventappluis370.ui.empresas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.EmpresaRequest
import kotlinx.coroutines.launch

@Composable
fun CreateEditEmpresaScreen(
    navController: NavController,
    viewModel: EmpresasViewModel = hiltViewModel(),
    empresaId: String? = null
) {
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val isEditing = empresaId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(empresaId) {
        if (isEditing) {
            viewModel.getEmpresaById(empresaId!!)
        }
    }

    val selectedEmpresa by viewModel.selectedEmpresa.collectAsState()
    LaunchedEffect(selectedEmpresa) {
        selectedEmpresa?.let {
            nombre = it.nombreEmpresa ?: ""
            direccion = it.direccion ?: ""
            telefono = it.telefono ?: ""
            email = it.email ?: ""
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is EmpresasUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
        if (currentState is EmpresasUiState.Error) {
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
            Text(
                text = if (isEditing) "Editar Empresa" else "Nueva Empresa",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la Empresa") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (email.isBlank()) {
                            snackbarHostState.showSnackbar("El email es requerido")
                            return@launch
                        }

                        val empresaRequest = EmpresaRequest(
                            nombreEmpresa = nombre,
                            direccion = direccion.ifBlank { null },
                            telefono = telefono.ifBlank { null },
                            email = email
                        )
                        if (isEditing) {
                            viewModel.updateEmpresa(empresaId!!, empresaRequest)
                        } else {
                            viewModel.createEmpresa(empresaRequest)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is EmpresasUiState.Loading
            ) {
                if (uiState is EmpresasUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}
