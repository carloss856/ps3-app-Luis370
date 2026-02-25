package com.example.inventappluis370.ui.equipos

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.EquipoRequest
import com.example.inventappluis370.data.model.Usuario
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.usuarios.UsuariosUiState
import com.example.inventappluis370.ui.usuarios.UsuariosViewModel
import kotlinx.coroutines.launch

private fun Usuario.matchesAssignedId(id: String): Boolean {
    if (id.isBlank()) return false
    val candidates = listOf(idPersona, idRaw, userIdRaw, mongoIdRaw)
        .mapNotNull { it?.trim() }
        .filter { it.isNotBlank() }
    return candidates.any { it == id }
}

private fun Usuario.displayNameWithRma(): String {
    val nombreResolved = nombre?.trim().orEmpty()
    val rmaCandidate = idPersona?.trim()
        .takeUnless { it.isNullOrBlank() }
        ?: userIdRaw?.trim().takeUnless { it.isNullOrBlank() }
        ?: idRaw?.trim().takeUnless { it.isNullOrBlank() }
        ?: mongoIdRaw?.trim().takeUnless { it.isNullOrBlank() }
    return when {
        nombreResolved.isNotBlank() && !rmaCandidate.isNullOrBlank() -> "$nombreResolved - RMA: $rmaCandidate"
        nombreResolved.isNotBlank() -> nombreResolved
        !rmaCandidate.isNullOrBlank() -> "RMA: $rmaCandidate"
        else -> "(Sin usuario)"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditEquipoScreen(
    navController: NavController,
    viewModel: EquiposViewModel = hiltViewModel(),
    usuariosViewModel: UsuariosViewModel = hiltViewModel(),
    equipoId: String? = null
) {
    var tipoEquipo by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var idAsignado by remember { mutableStateOf("") }
    var usuarioExpanded by remember { mutableStateOf(false) }
    var loadedFromSelected by remember(equipoId) { mutableStateOf(false) }

    // Texto mostrado en el dropdown (no necesariamente el ID)
    var usuarioAsignadoLabel by remember { mutableStateOf("") }

    val isEditing = equipoId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(equipoId) {
        // dropdown usuarios siempre
        usuariosViewModel.getUsers()

        viewModel.clearSelectedEquipo()
        if (isEditing) {
            viewModel.fetchEquipoById(equipoId!!)
        }
    }

    val selectedEquipo by viewModel.selectedEquipo.collectAsState()
    val selectedLoading by viewModel.selectedEquipoLoading.collectAsState()
    val selectedError by viewModel.selectedEquipoError.collectAsState()

    LaunchedEffect(selectedEquipo, isEditing, loadedFromSelected) {
        selectedEquipo?.let {
            if (isEditing && loadedFromSelected) return@let
            tipoEquipo = it.tipoEquipo ?: ""
            marca = it.marca ?: ""
            modelo = it.modelo ?: ""
            idAsignado = it.idAsignado ?: it.propiedad?.idPersona ?: ""
            if (isEditing) loadedFromSelected = true
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    // Cuando la operacion termina ok, volvemos al listado y pedimos refresh.
    LaunchedEffect(uiState) {
        if (uiState is EquiposUiState.OperationSuccess) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("refresh", true)
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("operation_message", if (isEditing) "Equipo editado correctamente" else "Equipo creado correctamente")
            navController.popBackStack()
        }
    }

    // Cargar usuarios para dropdown
    val usuariosState by usuariosViewModel.uiState.collectAsState()
    val usuarios: List<Usuario> = (usuariosState as? UsuariosUiState.Success)?.users.orEmpty()

    LaunchedEffect(idAsignado, usuarios) {
        if (idAsignado.isBlank()) {
            usuarioAsignadoLabel = ""
        } else {
            val u = usuarios.firstOrNull { it.matchesAssignedId(idAsignado.trim()) }
            val resolved = u?.displayNameWithRma().orEmpty()
            usuarioAsignadoLabel = if (resolved.isNotBlank()) resolved else idAsignado
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = if (isEditing) "Editar Equipo" else "Nuevo Equipo",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Devices,
                endIconContentDescription = "Equipos",
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

            if (isEditing && selectedLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (isEditing && !selectedError.isNullOrBlank()) {
                Text(
                    text = "No se pudo cargar el equipo: $selectedError",
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedTextField(
                value = tipoEquipo,
                onValueChange = { tipoEquipo = it },
                label = { Text("Tipo de Equipo *") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = marca,
                onValueChange = { marca = it },
                label = { Text("Marca") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = modelo,
                onValueChange = { modelo = it },
                label = { Text("Modelo") },
                modifier = Modifier.fillMaxWidth()
            )

            // Segun contrato/controller: el backend recibe id_asignado (id_persona del usuario asignado)
            ExposedDropdownMenuBox(
                expanded = usuarioExpanded,
                onExpandedChange = { usuarioExpanded = !usuarioExpanded },
            ) {
                OutlinedTextField(
                    value = usuarioAsignadoLabel,
                    onValueChange = { /* solo lectura: se selecciona desde el menu */ },
                    readOnly = true,
                    label = { Text("Usuario asignado *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        if (usuariosState is UsuariosUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = usuarioExpanded)
                        }
                    },
                    singleLine = true,
                )

                ExposedDropdownMenu(
                    expanded = usuarioExpanded,
                    onDismissRequest = { usuarioExpanded = false },
                ) {
                    if (usuarios.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                val msg = when (usuariosState) {
                                    is UsuariosUiState.Loading -> "Cargando usuarios..."
                                    is UsuariosUiState.Error -> "No se pudieron cargar usuarios"
                                    else -> "Sin usuarios"
                                }
                                Text(msg)
                            },
                            onClick = { usuarioExpanded = false },
                            enabled = false,
                        )
                    } else {
                        usuarios.forEach { u ->
                            val id = (u.idPersona ?: u.idRaw ?: "").trim()
                            val label = u.displayNameWithRma()
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    idAsignado = id
                                    usuarioAsignadoLabel = label
                                    usuarioExpanded = false
                                }
                            )
                        }
                    }
                }
            }

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
                        scope.launch {
                            if (tipoEquipo.isBlank()) {
                                snackbarHostState.showSnackbar("Tipo de equipo es requerido")
                                return@launch
                            }
                            if (idAsignado.isBlank()) {
                                snackbarHostState.showSnackbar("Debes seleccionar un usuario asignado")
                                return@launch
                            }

                            val equipoRequest = EquipoRequest(
                                tipoEquipo = tipoEquipo.trim(),
                                marca = marca.trim().ifBlank { null },
                                modelo = modelo.trim().ifBlank { null },
                                idPersona = null,
                                idAsignado = idAsignado.trim().ifBlank { null }
                            )

                            if (isEditing) {
                                viewModel.updateEquipo(equipoId!!, equipoRequest)
                            } else {
                                viewModel.createEquipo(equipoRequest)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is EquiposUiState.Loading && (!isEditing || !selectedLoading)
                ) {
                    if (uiState is EquiposUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (isEditing) "Actualizar" else "Guardar")
                    }
                }
            }
        }
    }
}

