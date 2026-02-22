package com.example.inventappluis370.ui.solicitudes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.CreateSolicitudRequest
import com.example.inventappluis370.data.model.Repuesto
import com.example.inventappluis370.data.model.Servicio
import com.example.inventappluis370.data.model.Usuario
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.repuestos.RepuestosUiState
import com.example.inventappluis370.ui.repuestos.RepuestosViewModel
import com.example.inventappluis370.ui.servicios.ServiciosUiState
import com.example.inventappluis370.ui.servicios.ServiciosViewModel
import com.example.inventappluis370.ui.usuarios.UsuariosUiState
import com.example.inventappluis370.ui.usuarios.UsuariosViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSolicitudScreen(
    navController: NavController,
    viewModel: SolicitudesRepuestoViewModel = hiltViewModel(),
    repuestosViewModel: RepuestosViewModel = hiltViewModel(),
    serviciosViewModel: ServiciosViewModel = hiltViewModel(),
    usuariosViewModel: UsuariosViewModel = hiltViewModel(),
) {
    var repuestoId by remember { mutableStateOf("") }
    var servicioId by remember { mutableStateOf("") }
    var idUsuario by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var comentarios by remember { mutableStateOf("") }

    var repuestoExpanded by remember { mutableStateOf(false) }
    var servicioExpanded by remember { mutableStateOf(false) }
    var usuarioExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is SolicitudesUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
    }

    // Cargar data para dropdowns
    val repuestosState by repuestosViewModel.uiState.collectAsState()
    val serviciosState by serviciosViewModel.uiState.collectAsState()
    val usuariosState by usuariosViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (repuestosState !is RepuestosUiState.Success) repuestosViewModel.getRepuestos()
        if (serviciosState !is ServiciosUiState.Success) serviciosViewModel.getServicios()
        if (usuariosState !is UsuariosUiState.Success) usuariosViewModel.getUsers()
    }

    val repuestos: List<Repuesto> = (repuestosState as? RepuestosUiState.Success)?.repuestos.orEmpty()
    val servicios: List<Servicio> = (serviciosState as? ServiciosUiState.Success)?.servicios.orEmpty()
    val usuarios: List<Usuario> = (usuariosState as? UsuariosUiState.Success)?.users.orEmpty()

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Nueva Solicitud de Repuesto",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.AddShoppingCart,
                endIconContentDescription = "Solicitudes",
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = repuestoExpanded,
                onExpandedChange = { repuestoExpanded = !repuestoExpanded }
            ) {
                OutlinedTextField(
                    value = repuestoId,
                    onValueChange = { repuestoId = it },
                    label = { Text("Repuesto *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = repuestoExpanded)
                    },
                    singleLine = true,
                )
                ExposedDropdownMenu(
                    expanded = repuestoExpanded,
                    onDismissRequest = { repuestoExpanded = false }
                ) {
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

            ExposedDropdownMenuBox(
                expanded = servicioExpanded,
                onExpandedChange = { servicioExpanded = !servicioExpanded }
            ) {
                OutlinedTextField(
                    value = servicioId,
                    onValueChange = { servicioId = it },
                    label = { Text("Servicio *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = servicioExpanded)
                    },
                    singleLine = true,
                )
                ExposedDropdownMenu(
                    expanded = servicioExpanded,
                    onDismissRequest = { servicioExpanded = false }
                ) {
                    servicios.forEach { s ->
                        val id = s.idServicio.orEmpty()
                        val estadoTxt = s.estado ?: ""
                        DropdownMenuItem(
                            text = { Text("$id — $estadoTxt") },
                            onClick = {
                                servicioId = id
                                servicioExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = usuarioExpanded,
                onExpandedChange = { usuarioExpanded = !usuarioExpanded }
            ) {
                OutlinedTextField(
                    value = idUsuario,
                    onValueChange = { idUsuario = it },
                    label = { Text("Usuario solicitante *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = usuarioExpanded)
                    },
                    singleLine = true,
                )
                ExposedDropdownMenu(
                    expanded = usuarioExpanded,
                    onDismissRequest = { usuarioExpanded = false }
                ) {
                    usuarios.forEach { u ->
                        val id = u.idPersona ?: u.idRaw ?: ""
                        val nombre = u.nombre ?: "(Sin nombre)"
                        DropdownMenuItem(
                            text = { Text("$id — $nombre") },
                            onClick = {
                                idUsuario = id
                                usuarioExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad solicitada *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = estado,
                onValueChange = { estado = it },
                label = { Text("Estado (Pendiente/Aprobada/Rechazada)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = comentarios,
                onValueChange = { comentarios = it },
                label = { Text("Comentarios (Opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val cantidadInt = cantidad.trim().toIntOrNull()
                    when {
                        repuestoId.isBlank() -> {
                            scope.launch { snackbarHostState.showSnackbar("Debes seleccionar un repuesto") }
                            return@Button
                        }
                        servicioId.isBlank() -> {
                            scope.launch { snackbarHostState.showSnackbar("Debes seleccionar un servicio") }
                            return@Button
                        }
                        idUsuario.isBlank() -> {
                            scope.launch { snackbarHostState.showSnackbar("Debes seleccionar un usuario") }
                            return@Button
                        }
                        cantidadInt == null -> {
                            scope.launch { snackbarHostState.showSnackbar("Cantidad invalida: usa solo numeros enteros") }
                            return@Button
                        }
                        cantidadInt <= 0 -> {
                            scope.launch { snackbarHostState.showSnackbar("La cantidad debe ser mayor que cero") }
                            return@Button
                        }
                    }
                    val cantidadSolicitada = cantidadInt!!

                    val request = CreateSolicitudRequest(
                        repuestoId = repuestoId.trim(),
                        servicioId = servicioId.trim(),
                        cantidadSolicitada = cantidadSolicitada,
                        idUsuario = idUsuario.trim(),
                        estadoSolicitud = estado.trim().ifBlank { null },
                        comentarios = comentarios.trim().ifBlank { null }
                    )
                    viewModel.createSolicitud(request)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is SolicitudesUiState.Loading
            ) {
                if (uiState is SolicitudesUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Crear Solicitud")
                }
            }
        }
    }
}
