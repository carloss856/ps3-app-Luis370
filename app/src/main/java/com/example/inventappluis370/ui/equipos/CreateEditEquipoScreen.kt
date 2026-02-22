package com.example.inventappluis370.ui.equipos

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

    LaunchedEffect(selectedEquipo) {
        selectedEquipo?.let {
            tipoEquipo = it.tipoEquipo ?: ""
            marca = it.marca ?: ""
            modelo = it.modelo ?: ""
            idAsignado = it.idAsignado ?: ""
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    // Cuando la operación termina ok, volvemos al listado y pedimos refresh.
    LaunchedEffect(uiState) {
        if (uiState is EquiposUiState.OperationSuccess) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("refresh", true)
            navController.popBackStack()
        }
    }

    // Cargar usuarios para dropdown
    val usuariosState by usuariosViewModel.uiState.collectAsState()
    val usuarios: List<Usuario> = (usuariosState as? UsuariosUiState.Success)?.users.orEmpty()

    LaunchedEffect(selectedEquipo, usuarios) {
        // Cuando estamos editando, si ya tenemos idAsignado y ya cargaron usuarios,
        // resolvemos el label para mostrarlo.
        val id = idAsignado
        if (id.isNotBlank() && usuarioAsignadoLabel.isBlank()) {
            val u = usuarios.firstOrNull { (it.idPersona ?: it.idRaw ?: "") == id }
            if (u != null) {
                val nombre = (u.nombre ?: "").trim()
                usuarioAsignadoLabel = if (nombre.isNotBlank()) nombre else id
            } else {
                // fallback: mostrar el ID si no se puede resolver
                usuarioAsignadoLabel = id
            }
        }
    }

    // Cuando el estado de los usuarios cambia, si ya tenemos un idAsignado,
    // tratamos de resolver el nombre para mostrar en el campo correspondiente.
    LaunchedEffect(usuariosState) {
        if (usuariosState is UsuariosUiState.Success) {
            val u = usuarios.firstOrNull { (it.idPersona ?: it.idRaw ?: "") == idAsignado }
            usuarioAsignadoLabel = u?.nombre ?: idAsignado
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
                .padding(16.dp),
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

            // Según contrato/controller: el backend recibe id_asignado (id_persona del usuario asignado)
            ExposedDropdownMenuBox(
                expanded = usuarioExpanded,
                onExpandedChange = { usuarioExpanded = !usuarioExpanded },
            ) {
                OutlinedTextField(
                    value = usuarioAsignadoLabel,
                    onValueChange = { /* solo lectura: se selecciona desde el menú */ },
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
                                    is UsuariosUiState.Loading -> "Cargando usuarios…"
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
                            val nombre = (u.nombre ?: "").trim()
                            val label = when {
                                nombre.isNotBlank() -> nombre
                                id.isNotBlank() -> id
                                else -> "(Sin usuario)"
                            }
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
                modifier = Modifier.fillMaxWidth(),
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
