package com.example.inventappluis370.ui.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.Empresa
import com.example.inventappluis370.data.model.UserRequest
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.empresas.EmpresasUiState
import com.example.inventappluis370.ui.empresas.EmpresasViewModel
import com.example.inventappluis370.ui.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditUsuarioScreen(
    navController: NavController,
    viewModel: UsuariosViewModel = hiltViewModel(),
    empresasViewModel: EmpresasViewModel = hiltViewModel(),
    userId: String? = null
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var rolExpanded by remember { mutableStateOf(false) }
    var idEmpresa by remember { mutableStateOf("") }
    var empresaLabel by remember { mutableStateOf("") }
    var empresaExpanded by remember { mutableStateOf(false) }
    var contrasena by remember { mutableStateOf("") }

    val isEditing = userId != null
    val roleOptions = listOf("Administrador", "Gerente", "Tecnico", "Cliente", "Empresa")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val empresasState by empresasViewModel.uiState.collectAsState()
    val empresas: List<Empresa> = (empresasState as? EmpresasUiState.Success)?.empresas.orEmpty()

    fun fieldError(key: String): String? = fieldErrors[key]?.firstOrNull()

    LaunchedEffect(userId) {
        if (isEditing) {
            viewModel.getUserById(userId!!)
        }
        if (empresasState !is EmpresasUiState.Success) {
            empresasViewModel.getEmpresas()
        }
    }

    val selectedUser by viewModel.selectedUser.collectAsState()
    LaunchedEffect(selectedUser) {
        selectedUser?.let {
            nombre = it.nombre ?: ""
            email = it.email ?: ""
            telefono = it.telefono?.filter { ch -> ch.isDigit() } ?: ""
            tipo = it.tipo ?: ""
            idEmpresa = it.idEmpresa ?: ""
            empresaLabel = empresas.firstOrNull { e -> e.idEmpresa == idEmpresa }?.nombreEmpresa ?: idEmpresa
        }
    }

    LaunchedEffect(idEmpresa, empresas) {
        if (idEmpresa.isNotBlank()) {
            empresaLabel = empresas.firstOrNull { e -> e.idEmpresa == idEmpresa }?.nombreEmpresa ?: idEmpresa
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is UsuariosUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.previousBackStackEntry?.savedStateHandle
                ?.set("operation_message", if (isEditing) "Usuario editado correctamente" else "Usuario creado correctamente")
            navController.popBackStack()
        }
        if (currentState is UsuariosUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(currentState.message)
            }
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = if (isEditing) "Editar Usuario" else "Nuevo Usuario",
                onBack = { navController.popBackStack() },
                endIcon = null,
                endIconContentDescription = null,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                    telefono = it.filter { ch -> ch.isDigit() }
                    viewModel.clearFieldErrors()
                },
                label = { Text("Telefono") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = fieldError("telefono") != null,
                supportingText = { fieldError("telefono")?.let { Text(it) } }
            )

            ExposedDropdownMenuBox(
                expanded = rolExpanded,
                onExpandedChange = { rolExpanded = !rolExpanded },
            ) {
                OutlinedTextField(
                    value = tipo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo (Rol)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolExpanded) },
                    isError = fieldError("tipo") != null,
                    supportingText = { fieldError("tipo")?.let { Text(it) } },
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = rolExpanded,
                    onDismissRequest = { rolExpanded = false }
                ) {
                    roleOptions.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = {
                                tipo = role
                                rolExpanded = false
                                viewModel.clearFieldErrors()
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = empresaExpanded,
                onExpandedChange = { empresaExpanded = !empresaExpanded },
            ) {
                OutlinedTextField(
                    value = empresaLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Empresa (opcional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        if (empresasState is EmpresasUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = empresaExpanded)
                        }
                    },
                    isError = fieldError("id_empresa") != null,
                    supportingText = { fieldError("id_empresa")?.let { Text(it) } },
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = empresaExpanded,
                    onDismissRequest = { empresaExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin empresa") },
                        onClick = {
                            idEmpresa = ""
                            empresaLabel = ""
                            empresaExpanded = false
                            viewModel.clearFieldErrors()
                        }
                    )
                    empresas.forEach { e ->
                        val id = e.idEmpresa.orEmpty()
                        val label = e.nombreEmpresa ?: id
                        if (id.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    idEmpresa = id
                                    empresaLabel = label
                                    empresaExpanded = false
                                    viewModel.clearFieldErrors()
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = contrasena,
                onValueChange = {
                    contrasena = it
                    viewModel.clearFieldErrors()
                },
                label = { Text(if (isEditing) "Nueva Contrasena (Opcional)" else "Contrasena") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = fieldError("contrasena") != null,
                supportingText = { fieldError("contrasena")?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Volver")
                }

                Button(
                    onClick = {
                        val userRequest = UserRequest(
                            nombre = nombre.trim(),
                            email = email.trim(),
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
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is UsuariosUiState.Loading
                ) {
                    if (uiState is UsuariosUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Guardar")
                    }
                }
            }

            if (isEditing) {
                OutlinedButton(
                    onClick = {
                        val route = Routes.PERMISOS_USER.replace("{userId}", userId!!)
                        navController.navigate(route)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Permisos de este usuario")
                }
            }
        }
    }
}





