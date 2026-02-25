package com.example.inventappluis370.ui.empresas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.EmpresaRequest
import com.example.inventappluis370.ui.common.FormFieldState
import com.example.inventappluis370.ui.common.ModuleTopBar
import kotlinx.coroutines.launch

@Composable
fun CreateEditEmpresaScreen(
    navController: NavController,
    viewModel: EmpresasViewModel = hiltViewModel(),
    empresaId: String? = null
) {
    var nombreState by remember { mutableStateOf(FormFieldState()) }
    var direccionState by remember { mutableStateOf(FormFieldState()) }
    var telefonoState by remember { mutableStateOf(FormFieldState()) }
    var emailState by remember { mutableStateOf(FormFieldState()) }

    val isEditing = empresaId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Errores por campo provenientes del backend (422)
    val fieldErrors by viewModel.fieldErrors.collectAsState()

    fun clearErrors() {
        nombreState = nombreState.copy(error = null)
        direccionState = direccionState.copy(error = null)
        telefonoState = telefonoState.copy(error = null)
        emailState = emailState.copy(error = null)
    }

    fun applyBackendFieldErrors(errors: Map<String, List<String>>) {
        fun firstMsg(vararg keys: String): String? {
            for (k in keys) {
                val v = errors[k]
                if (!v.isNullOrEmpty()) return v.firstOrNull()
            }
            return null
        }

        // keys esperadas segun contrato Laravel: snake_case
        firstMsg("nombre_empresa", "nombreEmpresa", "nombre", "nombre de la empresa")?.let { msg ->
            nombreState = nombreState.copy(error = msg)
        }
        firstMsg("direccion", "direccion")?.let { msg ->
            direccionState = direccionState.copy(error = msg)
        }
        firstMsg("telefono", "telefono")?.let { msg ->
            telefonoState = telefonoState.copy(error = msg)
        }
        firstMsg("email", "correo")?.let { msg ->
            emailState = emailState.copy(error = msg)
        }
    }

    fun applyBackendErrorToFields(message: String) {
        // Si el backend manda "validation.required" sin field,
        // resaltamos obligatorios SOLO si estan vacios.
        if (
            message.contains("required", ignoreCase = true) ||
            message.contains("validation.required", ignoreCase = true)
        ) {
            if (nombreState.value.isBlank()) nombreState = nombreState.copy(error = "Obligatorio")
            if (emailState.value.isBlank()) emailState = emailState.copy(error = "Obligatorio")
        }

        // Heuristica para mensajes que incluyan nombre de campo:
        // no sobreescribas con "Revisa" si ya hay un error mas especifico.
        if (message.contains("nombre", ignoreCase = true) && nombreState.error == null) {
            nombreState = nombreState.copy(error = "Revisa este campo")
        }
        if (message.contains("email", ignoreCase = true) && emailState.error == null) {
            emailState = emailState.copy(error = "Revisa este campo")
        }
    }

    LaunchedEffect(empresaId) {
        if (isEditing) {
            viewModel.getEmpresaById(empresaId!!)
        }
    }

    val selectedEmpresa by viewModel.selectedEmpresa.collectAsState()
    LaunchedEffect(selectedEmpresa) {
        selectedEmpresa?.let {
            nombreState = nombreState.copy(value = it.nombreEmpresa.orEmpty(), error = null)
            direccionState = direccionState.copy(value = it.direccion.orEmpty(), error = null)
            telefonoState = telefonoState.copy(value = it.telefono.orEmpty(), error = null)
            emailState = emailState.copy(value = it.email.orEmpty(), error = null)
        }
    }

    val operationSuccess by viewModel.operationSuccess.collectAsState()
    LaunchedEffect(operationSuccess) {
        if (operationSuccess) {
            // Marca refresh ANTES de volver.
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.previousBackStackEntry?.savedStateHandle?.set(
                "operation_message",
                if (isEditing) "Empresa editada correctamente" else "Empresa creada correctamente"
            )
            viewModel.consumeOperationSuccess()
            navController.popBackStack()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is EmpresasUiState.Error) {
            // Si el backend devolvio un error generico, intentamos reflejar en campos.
            applyBackendErrorToFields(currentState.message)
            scope.launch {
                snackbarHostState.showSnackbar(currentState.message)
            }
        }
    }

    LaunchedEffect(fieldErrors) {
        if (fieldErrors.isNotEmpty()) {
            // Limpia errores anteriores antes de aplicar del backend
            clearErrors()
            applyBackendFieldErrors(fieldErrors)
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = if (isEditing) "Editar Empresa" else "Nueva Empresa",
                endIcon = Icons.Default.Business,
                endIconContentDescription = "Empresa",
                onBack = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = nombreState.value,
                onValueChange = {
                    nombreState = nombreState.copy(value = it, error = null)
                    // Si habia error backend de este campo, lo consumimos al editar.
                    if (fieldErrors.isNotEmpty()) viewModel.clearFieldError("nombre_empresa")
                },
                label = { Text("Nombre de la Empresa *") },
                modifier = Modifier.fillMaxWidth(),
                isError = nombreState.error != null,
                supportingText = {
                    val err = nombreState.error
                    if (err != null) Text(err)
                }
            )
            OutlinedTextField(
                value = direccionState.value,
                onValueChange = {
                    direccionState = direccionState.copy(value = it, error = null)
                    if (fieldErrors.isNotEmpty()) viewModel.clearFieldError("direccion")
                },
                label = { Text("Direccion") },
                modifier = Modifier.fillMaxWidth(),
                isError = direccionState.error != null,
                supportingText = {
                    val err = direccionState.error
                    if (err != null) Text(err)
                }
            )
            OutlinedTextField(
                value = telefonoState.value,
                onValueChange = {
                    telefonoState = telefonoState.copy(value = it, error = null)
                    if (fieldErrors.isNotEmpty()) viewModel.clearFieldError("telefono")
                },
                label = { Text("Telefono") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = telefonoState.error != null,
                supportingText = {
                    val err = telefonoState.error
                    if (err != null) Text(err)
                }
            )
            OutlinedTextField(
                value = emailState.value,
                onValueChange = {
                    emailState = emailState.copy(value = it, error = null)
                    if (fieldErrors.isNotEmpty()) viewModel.clearFieldError("email")
                },
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailState.error != null,
                supportingText = {
                    val err = emailState.error
                    if (err != null) Text(err)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Volver")
                }

                Button(
                    onClick = {
                        scope.launch {
                            focusManager.clearFocus()
                            clearErrors()

                            var hasErrors = false
                            if (nombreState.value.isBlank()) {
                                nombreState = nombreState.copy(error = "Obligatorio")
                                hasErrors = true
                            }
                            if (emailState.value.isBlank()) {
                                emailState = emailState.copy(error = "Obligatorio")
                                hasErrors = true
                            }
                            if (hasErrors) {
                                snackbarHostState.showSnackbar("Verifica los campos obligatorios marcados con *")
                                return@launch
                            }

                            val direccion = direccionState.value.trim().ifBlank { null }
                            val telefono = telefonoState.value.trim().ifBlank { null }

                            val empresaRequest = EmpresaRequest(
                                nombreEmpresa = nombreState.value.trim(),
                                direccion = direccion,
                                telefono = telefono,
                                email = emailState.value.trim(),
                                // MUY IMPORTANTE: no enviar fecha_creacion. En el backend puede romper validacion en update.
                                fechaCreacion = null
                            )

                            if (isEditing) {
                                viewModel.updateEmpresa(empresaId!!, empresaRequest)
                            } else {
                                viewModel.createEmpresa(empresaRequest)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is EmpresasUiState.Loading
                ) {
                    if (uiState is EmpresasUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (isEditing) "Actualizar" else "Guardar")
                    }
                }
            }
        }
    }
}



