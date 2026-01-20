package com.example.inventappluis370.ui.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.UserRequest
import kotlinx.coroutines.launch

@Composable
fun CreateEditUsuarioScreen(
    navController: NavController,
    viewModel: UsuariosViewModel = hiltViewModel(),
    userId: String? = null
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var idEmpresa by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    val isEditing = userId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val fieldErrors by viewModel.fieldErrors.collectAsState()

    fun fieldError(key: String): String? = fieldErrors[key]?.firstOrNull()

    LaunchedEffect(userId) {
        if (isEditing) {
            viewModel.getUserById(userId!!)
        }
    }

    val selectedUser by viewModel.selectedUser.collectAsState()
    LaunchedEffect(selectedUser) {
        selectedUser?.let {
            nombre = it.nombre ?: ""
            email = it.email ?: ""
            telefono = it.telefono ?: ""
            tipo = it.tipo ?: ""
            idEmpresa = it.idEmpresa ?: ""
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is UsuariosUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
        if (currentState is UsuariosUiState.Error) {
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
                text = if (isEditing) "Editar Usuario" else "Nuevo Usuario",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                isError = fieldError("nombre") != null,
                supportingText = { fieldError("nombre")?.let { Text(it) } }
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                isError = fieldError("email") != null,
                supportingText = { fieldError("email")?.let { Text(it) } }
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    telefono = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                isError = fieldError("telefono") != null,
                supportingText = { fieldError("telefono")?.let { Text(it) } }
            )

            OutlinedTextField(
                value = tipo,
                onValueChange = {
                    tipo = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Tipo (Rol)") },
                modifier = Modifier.fillMaxWidth(),
                isError = fieldError("tipo") != null,
                supportingText = { fieldError("tipo")?.let { Text(it) } }
            )

            OutlinedTextField(
                value = idEmpresa,
                onValueChange = {
                    idEmpresa = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("ID de Empresa (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                isError = fieldError("id_empresa") != null,
                supportingText = { fieldError("id_empresa")?.let { Text(it) } }
            )

            OutlinedTextField(
                value = contrasena,
                onValueChange = {
                    contrasena = it
                    viewModel.clearFieldErrors()
                },
                label = { Text(if (isEditing) "Nueva Contraseña (Opcional)" else "Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = fieldError("contrasena") != null,
                supportingText = { fieldError("contrasena")?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val userRequest = UserRequest(
                        nombre = nombre,
                        email = email,
                        telefono = telefono.ifBlank { null },
                        tipo = tipo,
                        idEmpresa = idEmpresa.ifBlank { null },
                        contrasena = contrasena.ifBlank { null }
                    )
                    if (isEditing) {
                        viewModel.updateUser(userId!!, userRequest)
                    } else {
                        viewModel.createUser(userRequest)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is UsuariosUiState.Loading
            ) {
                if (uiState is UsuariosUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}
