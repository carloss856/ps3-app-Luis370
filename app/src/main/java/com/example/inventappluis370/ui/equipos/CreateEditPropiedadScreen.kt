package com.example.inventappluis370.ui.equipos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.PropiedadEquipoRequest
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.usuarios.UsuariosUiState
import com.example.inventappluis370.ui.usuarios.UsuariosViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditPropiedadScreen(
    navController: NavController,
    viewModel: PropiedadEquipoViewModel = hiltViewModel(),
    equiposViewModel: EquiposViewModel = hiltViewModel(),
    usuariosViewModel: UsuariosViewModel = hiltViewModel(),
    propiedadId: String? = null
) {
    var equipoId by remember { mutableStateOf("") }
    var personaId by remember { mutableStateOf("") }
    var equipoLabel by remember { mutableStateOf("") }
    var personaLabel by remember { mutableStateOf("") }
    var equipoExpanded by remember { mutableStateOf(false) }
    var personaExpanded by remember { mutableStateOf(false) }

    val isEditing = propiedadId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val equiposState by equiposViewModel.uiState.collectAsState()
    val usuariosState by usuariosViewModel.uiState.collectAsState()
    val equipos = (equiposState as? EquiposUiState.Success)?.equipos.orEmpty()
    val usuarios = (usuariosState as? UsuariosUiState.Success)?.users.orEmpty()

    LaunchedEffect(Unit) {
        equiposViewModel.getEquipos()
        usuariosViewModel.getUsers()
    }

    LaunchedEffect(propiedadId) {
        if (isEditing) {
            viewModel.getPropiedadById(propiedadId!!)
        }
    }

    val selected by viewModel.selectedPropiedad.collectAsState()
    LaunchedEffect(selected, equipos, usuarios) {
        selected?.let {
            equipoId = it.idEquipo ?: ""
            personaId = it.idPersona ?: ""
            equipoLabel = equipos.firstOrNull { eq -> eq.idEquipo == equipoId }?.let { eq ->
                listOfNotNull(eq.tipoEquipo, eq.marca, eq.modelo).joinToString(" ").ifBlank { equipoId }
            } ?: equipoId
            personaLabel = usuarios.firstOrNull { us -> (us.idPersona ?: us.idRaw ?: "") == personaId }?.nombre
                ?: personaId
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is PropiedadEquipoUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.previousBackStackEntry?.savedStateHandle?.set(
                "operation_message",
                if (isEditing) "Asignacion editada correctamente" else "Asignacion creada correctamente"
            )
            navController.popBackStack()
        }
        if (uiState is PropiedadEquipoUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((uiState as PropiedadEquipoUiState.Error).message)
            }
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = if (isEditing) "Editar asignacion" else "Nueva asignacion",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Link,
                endIconContentDescription = "Asignacion"
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipoExpanded) },
                    singleLine = true
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
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = personaExpanded,
                onExpandedChange = { personaExpanded = !personaExpanded }
            ) {
                OutlinedTextField(
                    value = personaLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dueno *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = personaExpanded) },
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = personaExpanded,
                    onDismissRequest = { personaExpanded = false }
                ) {
                    usuarios.forEach { u ->
                        val id = u.idPersona ?: u.idRaw ?: ""
                        val label = u.nombre ?: id
                        if (id.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    personaId = id
                                    personaLabel = label
                                    personaExpanded = false
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
                        if (equipoId.isBlank() || personaId.isBlank()) {
                            snackbarHostState.showSnackbar("Debes seleccionar equipo y dueno")
                            return@launch
                        }

                        val request = PropiedadEquipoRequest(
                            idEquipo = equipoId.trim(),
                            idPersona = personaId.trim()
                        )
                        if (isEditing) {
                            viewModel.updatePropiedad(propiedadId!!, request)
                        } else {
                            viewModel.createPropiedad(request)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is PropiedadEquipoUiState.Loading
            ) {
                if (uiState is PropiedadEquipoUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isEditing) "Actualizar" else "Guardar")
                }
            }
        }
    }
}



