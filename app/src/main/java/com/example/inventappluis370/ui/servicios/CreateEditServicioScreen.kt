package com.example.inventappluis370.ui.servicios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.CreateServicioRequest
import com.example.inventappluis370.data.model.Equipo
import com.example.inventappluis370.data.model.Usuario
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.equipos.EquiposUiState
import com.example.inventappluis370.ui.equipos.EquiposViewModel
import com.example.inventappluis370.ui.rma.RmaUiState
import com.example.inventappluis370.ui.rma.RmaViewModel
import com.example.inventappluis370.ui.usuarios.UsuariosUiState
import com.example.inventappluis370.ui.usuarios.UsuariosViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditServicioScreen(
    navController: NavController,
    viewModel: ServiciosViewModel = hiltViewModel(),
    equiposViewModel: EquiposViewModel = hiltViewModel(),
    usuariosViewModel: UsuariosViewModel = hiltViewModel(),
    rmaViewModel: RmaViewModel = hiltViewModel(),
    servicioId: String? = null
) {
    val today = remember { LocalDate.now().toString() }
    var equipoId by remember { mutableStateOf("") }
    var equipoLabel by remember { mutableStateOf("") }
    var equipoExpanded by remember { mutableStateOf(false) }
    var usuarioId by remember { mutableStateOf("") }
    var usuarioLabel by remember { mutableStateOf("") }
    var usuarioExpanded by remember { mutableStateOf(false) }
    var problemaReportado by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("Pendiente") }
    var estadoExpanded by remember { mutableStateOf(false) }
    var codigoRma by remember { mutableStateOf("") }
    var fechaIngreso by remember { mutableStateOf(today) }
    var costoEstimado by remember { mutableStateOf("") }
    var validadoPorGerente by remember { mutableStateOf(false) }

    val isEditing = servicioId != null
    val canEditEstado = viewModel.canEditEstado()
    val estadoOptions = listOf("Pendiente", "En proceso", "Finalizado", "Cancelado")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val fieldErrors by viewModel.fieldErrors.collectAsState()
    fun fieldError(key: String): String? = fieldErrors[key]?.firstOrNull()

    LaunchedEffect(servicioId) {
        if (isEditing) {
            viewModel.getServicioById(servicioId!!)
        }
    }

    val selectedServicio by viewModel.selectedServicio.collectAsState()

    // Cargar equipos para dropdown
    val equiposState by equiposViewModel.uiState.collectAsState()
    val usuariosState by usuariosViewModel.uiState.collectAsState()
    val rmaState by rmaViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        if (equiposState !is EquiposUiState.Success) {
            equiposViewModel.getEquipos()
        }
        if (usuariosState !is UsuariosUiState.Success) {
            usuariosViewModel.getUsers()
        }
    }
    val equipos: List<Equipo> = (equiposState as? EquiposUiState.Success)?.equipos.orEmpty()
    val usuarios: List<Usuario> = (usuariosState as? UsuariosUiState.Success)?.users.orEmpty()
    val rmas = (rmaState as? RmaUiState.Success)?.rmas.orEmpty()
    val rmaByUserId = remember(rmas) {
        rmas.groupBy { it.idPersona.orEmpty() }
            .mapValues { entry -> entry.value.mapNotNull { it.rma }.distinct() }
    }

    fun usuarioLabelFor(id: String): String {
        if (id.isBlank()) return ""
        val user = usuarios.firstOrNull { u ->
            val uid = u.idPersona ?: u.idRaw ?: ""
            uid == id
        }
        val nombre = user?.nombre ?: id
        val rma = rmaByUserId[id]?.firstOrNull().orEmpty()
        return if (rma.isNotBlank()) "$nombre - RMA $rma" else nombre
    }
    fun formatCost(value: Double?): String {
        if (value == null) return ""
        return BigDecimal(value).stripTrailingZeros().toPlainString()
    }

    LaunchedEffect(selectedServicio, equipos, usuarios) {
        selectedServicio?.let {
            equipoId = it.idEquipo ?: ""
            problemaReportado = it.problemaReportado ?: ""
            estado = it.estado ?: "Pendiente"
            usuarioId = it.idUsuario ?: ""
            codigoRma = it.codigoRma ?: ""
            fechaIngreso = it.fechaIngreso ?: today
            costoEstimado = formatCost(it.costoEstimado)
            validadoPorGerente = it.validadoPorGerente ?: false
            equipoLabel = equipos.firstOrNull { e -> e.idEquipo == equipoId }?.let { e ->
                listOfNotNull(e.tipoEquipo, e.marca, e.modelo).joinToString(" ").ifBlank { equipoId }
            } ?: equipoId
            usuarioLabel = usuarioLabelFor(usuarioId)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is ServiciosUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.previousBackStackEntry?.savedStateHandle?.set(
                "operation_message",
                if (isEditing) "Servicio editado correctamente" else "Servicio creado correctamente"
            )
            navController.popBackStack()
        }
        if (uiState is ServiciosUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((uiState as ServiciosUiState.Error).message)
            }
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = if (isEditing) "Editar Servicio" else "Nuevo Servicio",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Build,
                endIconContentDescription = "Servicios",
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
            ExposedDropdownMenuBox(
                expanded = equipoExpanded,
                onExpandedChange = { equipoExpanded = !equipoExpanded }
            ) {
                OutlinedTextField(
                    value = equipoLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Equipo *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        if (equiposState is EquiposUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.height(18.dp))
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipoExpanded)
                        }
                    },
                    singleLine = true,
                    isError = fieldError("id_equipo") != null,
                    supportingText = { fieldError("id_equipo")?.let { Text(it) } }
                )

                ExposedDropdownMenu(
                    expanded = equipoExpanded,
                    onDismissRequest = { equipoExpanded = false }
                ) {
                    equipos.forEach { e ->
                        val id = e.idEquipo.orEmpty()
                        val label = listOfNotNull(e.tipoEquipo, e.marca, e.modelo).joinToString(" ").ifBlank { id }
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                equipoId = id
                                equipoLabel = label
                                equipoExpanded = false
                                viewModel.clearFieldErrors()
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
                    label = { Text("Usuario *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        if (usuariosState is UsuariosUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.height(18.dp))
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = usuarioExpanded)
                        }
                    },
                    singleLine = true,
                    isError = fieldError("id_usuario") != null,
                    supportingText = { fieldError("id_usuario")?.let { Text(it) } }
                )

                ExposedDropdownMenu(
                    expanded = usuarioExpanded,
                    onDismissRequest = { usuarioExpanded = false }
                ) {
                    usuarios.forEach { u ->
                        val id = u.idPersona ?: u.idRaw ?: ""
                        if (id.isBlank()) return@forEach
                        val label = usuarioLabelFor(id)
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                usuarioId = id
                                usuarioLabel = label
                                codigoRma = rmaByUserId[id]?.firstOrNull().orEmpty()
                                usuarioExpanded = false
                                viewModel.clearFieldErrors()
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = problemaReportado,
                onValueChange = {
                    problemaReportado = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Problema reportado") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                isError = fieldError("problema_reportado") != null,
                supportingText = { fieldError("problema_reportado")?.let { Text(it) } }
            )

            OutlinedTextField(
                value = fechaIngreso,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de ingreso") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = estadoExpanded,
                onExpandedChange = {
                    if (canEditEstado) estadoExpanded = !estadoExpanded
                }
            ) {
                OutlinedTextField(
                    value = estado,
                    onValueChange = {},
                    readOnly = true,
                    enabled = canEditEstado,
                    label = { Text("Estado") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        if (canEditEstado) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded)
                        }
                    },
                    isError = fieldError("estado") != null,
                    supportingText = {
                        val msg = fieldError("estado")
                            ?: if (!canEditEstado) "Solo Administrador o Gerente puede modificar estado" else null
                        msg?.let { Text(it) }
                    },
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = canEditEstado && estadoExpanded,
                    onDismissRequest = { estadoExpanded = false }
                ) {
                    estadoOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                estado = option
                                estadoExpanded = false
                                viewModel.clearFieldErrors()
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = costoEstimado,
                onValueChange = {
                    costoEstimado = it.filter { ch -> ch.isDigit() || ch == '.' }
                    viewModel.clearFieldErrors()
                },
                label = { Text("Costo estimado") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = fieldError("costo_estimado") != null,
                supportingText = { fieldError("costo_estimado")?.let { Text(it) } }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Validado por gerente")
                Checkbox(
                    checked = validadoPorGerente,
                    onCheckedChange = { validadoPorGerente = it }
                )
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
                        if (equipoId.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Debes seleccionar un equipo") }
                            return@Button
                        }
                        if (usuarioId.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Debes seleccionar un usuario") }
                            return@Button
                        }
                        val costoEstimadoValue = costoEstimado.toDoubleOrNull()
                        val request = CreateServicioRequest(
                            idEquipo = equipoId.trim(),
                            idUsuario = usuarioId.trim(),
                            codigoRma = codigoRma.trim(),
                            fechaIngreso = fechaIngreso.trim(),
                            problemaReportado = problemaReportado.trim(),
                            estado = estado.trim().ifBlank { "Pendiente" },
                            costoEstimado = costoEstimadoValue,
                            costoReal = null,
                            validadoPorGerente = validadoPorGerente
                        )
                        if (isEditing) {
                            viewModel.updateServicio(servicioId!!, request)
                        } else {
                            viewModel.createServicio(request)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is ServiciosUiState.Loading
                ) {
                    if (uiState is ServiciosUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (isEditing) "Actualizar" else "Guardar")
                    }
                }
            }
        }
    }
}

