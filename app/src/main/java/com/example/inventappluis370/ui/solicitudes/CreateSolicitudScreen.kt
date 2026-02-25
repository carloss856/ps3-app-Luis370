package com.example.inventappluis370.ui.solicitudes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
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
import com.example.inventappluis370.data.model.CreateSolicitudRequest
import com.example.inventappluis370.data.model.Repuesto
import com.example.inventappluis370.data.model.Servicio
import com.example.inventappluis370.data.model.UpdateSolicitudRequest
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
    solicitudId: String? = null,
) {
    val isEditing = !solicitudId.isNullOrBlank()

    var repuestoId by remember { mutableStateOf("") }
    var repuestoLabel by remember { mutableStateOf("") }
    var servicioId by remember { mutableStateOf("") }
    var servicioLabel by remember { mutableStateOf("") }
    var idUsuario by remember { mutableStateOf("") }
    var usuarioLabel by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var comentarios by remember { mutableStateOf("") }

    var repuestoExpanded by remember { mutableStateOf(false) }
    var servicioExpanded by remember { mutableStateOf(false) }
    var usuarioExpanded by remember { mutableStateOf(false) }
    var estadoExpanded by remember { mutableStateOf(false) }
    val estadoBaseOptions = listOf(
        "Pendiente",
        "Aprobada",
        "En proceso",
        "Completada",
        "Rechazada",
        "Cancelada"
    )
    val estadoOptions = remember(estado) {
        if (estado.isNotBlank() && estado !in estadoBaseOptions) estadoBaseOptions + estado else estadoBaseOptions
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var errorDialog by remember { mutableStateOf<String?>(null) }
    var successDialog by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    val selectedSolicitud by viewModel.selectedSolicitud.collectAsState()

    LaunchedEffect(solicitudId) {
        if (isEditing) viewModel.getSolicitudById(solicitudId!!)
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

    LaunchedEffect(selectedSolicitud, repuestos, servicios, usuarios) {
        val s = selectedSolicitud ?: return@LaunchedEffect
        if (!isEditing) return@LaunchedEffect

        repuestoId = s.idRepuesto ?: ""
        servicioId = s.idServicio ?: ""
        idUsuario = s.idUsuario ?: ""
        cantidad = (s.cantidadSolicitada ?: "").toString()
        estado = s.estadoSolicitud.orEmpty()
        comentarios = s.comentarios.orEmpty()

        repuestoLabel = repuestos.firstOrNull { it.idRepuesto == repuestoId }?.nombreRepuesto
            ?: repuestoId
        servicioLabel = servicios.firstOrNull { it.idServicio == servicioId }?.let {
            val tipo = it.problemaReportado?.takeIf { txt -> txt.isNotBlank() } ?: (it.estado ?: "Servicio")
            "$tipo"
        } ?: servicioId
        usuarioLabel = usuarios.firstOrNull { (it.idPersona ?: it.idRaw ?: "") == idUsuario }?.nombre
            ?: idUsuario
    }

    LaunchedEffect(uiState) {
        if (uiState is SolicitudesUiState.OperationSuccess) {
            successDialog = if (isEditing) "Solicitud editada correctamente" else "Solicitud creada correctamente"
        }
        if (uiState is SolicitudesUiState.Error) {
            errorDialog = (uiState as SolicitudesUiState.Error).message
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = if (isEditing) "Editar Solicitud" else "Nueva Solicitud de Repuesto",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = repuestoExpanded,
                onExpandedChange = { repuestoExpanded = !repuestoExpanded }
            ) {
                OutlinedTextField(
                    value = repuestoLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Repuesto *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repuestoExpanded) },
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

            ExposedDropdownMenuBox(
                expanded = servicioExpanded,
                onExpandedChange = { servicioExpanded = !servicioExpanded }
            ) {
                OutlinedTextField(
                    value = servicioLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Servicio *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = servicioExpanded) },
                    singleLine = true,
                )
                ExposedDropdownMenu(
                    expanded = servicioExpanded,
                    onDismissRequest = { servicioExpanded = false }
                ) {
                    servicios.forEach { s ->
                        val id = s.idServicio.orEmpty()
                        val label = s.problemaReportado?.takeIf { it.isNotBlank() }
                            ?: (s.estado ?: "Servicio $id")
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                servicioId = id
                                servicioLabel = label
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
                    value = usuarioLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Usuario solicitante *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = usuarioExpanded) },
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
                            text = { Text(nombre) },
                            onClick = {
                                idUsuario = id
                                usuarioLabel = nombre
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

            ExposedDropdownMenuBox(
                expanded = estadoExpanded,
                onExpandedChange = { estadoExpanded = !estadoExpanded }
            ) {
                OutlinedTextField(
                    value = estado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                    singleLine = true,
                )
                ExposedDropdownMenu(
                    expanded = estadoExpanded,
                    onDismissRequest = { estadoExpanded = false }
                ) {
                    estadoOptions.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                estado = item
                                estadoExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = comentarios,
                onValueChange = { comentarios = it },
                label = { Text("Comentarios") },
                modifier = Modifier.fillMaxWidth()
            )

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
                    if (repuestoId.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Debes seleccionar un repuesto") }
                        return@Button
                    }
                    if (servicioId.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Debes seleccionar un servicio") }
                        return@Button
                    }
                    if (idUsuario.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Debes seleccionar un usuario") }
                        return@Button
                    }
                    val cantidadInt = cantidad.trim().toIntOrNull()
                    if (cantidadInt == null) {
                        scope.launch { snackbarHostState.showSnackbar("Cantidad invalida: usa solo numeros enteros") }
                        return@Button
                    }
                    if (cantidadInt <= 0) {
                        scope.launch { snackbarHostState.showSnackbar("La cantidad debe ser mayor que cero") }
                        return@Button
                    }
                    val estadoValue = estado.trim().ifBlank { "Pendiente" }
                    val comentariosValue = comentarios.trim().ifBlank { null }

                    if (isEditing) {
                        viewModel.updateSolicitud(
                            solicitudId!!,
                            UpdateSolicitudRequest(
                                repuestoId = repuestoId.trim(),
                                servicioId = servicioId.trim(),
                                cantidadSolicitada = cantidadInt,
                                idUsuario = idUsuario.trim(),
                                estadoSolicitud = estadoValue,
                                comentarios = comentariosValue
                            )
                        )
                    } else {
                        viewModel.createSolicitud(
                            CreateSolicitudRequest(
                                repuestoId = repuestoId.trim(),
                                servicioId = servicioId.trim(),
                                cantidadSolicitada = cantidadInt,
                                idUsuario = idUsuario.trim(),
                                estadoSolicitud = estadoValue,
                                comentarios = comentariosValue
                            )
                        )
                    }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is SolicitudesUiState.Loading
                ) {
                    if (uiState is SolicitudesUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (isEditing) "Actualizar Solicitud" else "Crear Solicitud")
                    }
                }
            }
        }

        if (errorDialog != null) {
            AlertDialog(
                onDismissRequest = { errorDialog = null },
                confirmButton = { TextButton(onClick = { errorDialog = null }) { Text("Aceptar") } },
                title = { Text("Error") },
                text = { Text(errorDialog!!) }
            )
        }

        if (successDialog != null) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    TextButton(onClick = {
                        val msg = successDialog!!
                        successDialog = null
                        navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                        navController.previousBackStackEntry?.savedStateHandle?.set("operation_message", msg)
                        navController.popBackStack()
                    }) { Text("Aceptar") }
                },
                title = { Text("Operacion completada") },
                text = { Text(successDialog!!) }
            )
        }
    }
}



