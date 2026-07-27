package com.example.inventappluis370.ui.permisos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer
import com.example.inventappluis370.ui.usuarios.UsuariosUiState
import com.example.inventappluis370.ui.usuarios.UsuariosViewModel

private val ACTIONS = listOf("index", "store", "update", "destroy")

private fun actionLabel(action: String): String = when (action.lowercase()) {
    "index" -> "Ver"
    "store" -> "Crear"
    "update" -> "Editar"
    "destroy" -> "Eliminar"
    else -> action
}

private data class UserOption(
    val id: String,
    val name: String,
    val role: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermisosScreen(
    navController: NavController,
    viewModel: PermisosViewModel = hiltViewModel(),
    usuariosViewModel: UsuariosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val draft by viewModel.draftModules.collectAsState()
    val usuariosState by usuariosViewModel.uiState.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val userIdArg = remember(backStackEntry) { backStackEntry?.arguments?.getString("userId") }

    var selectedUserId by remember(userIdArg) {
        mutableStateOf(userIdArg?.takeIf { it.isNotBlank() })
    }
    var selectedRole by remember { mutableStateOf(PermisosViewModel.DEFAULT_ROLE) }
    var userExpanded by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }

    val userOptions = (usuariosState as? UsuariosUiState.Success)
        ?.users
        ?.mapNotNull { u ->
            val id = u.idPersona ?: u.idRaw ?: return@mapNotNull null
            val name = u.nombre ?: id
            val role = u.tipo?.takeIf { it.isNotBlank() } ?: "Sin rol"
            UserOption(id = id, name = name, role = role)
        }
        ?.sortedBy { it.name.lowercase() }
        .orEmpty()

    val roles = listOf("Todos") + userOptions.map { it.role }.distinct().sorted()
    val filteredUsers = if (selectedRole == "Todos") userOptions else userOptions.filter { it.role == selectedRole }

    val selectedUserName = userOptions.firstOrNull { it.id == selectedUserId }?.name ?: selectedUserId

    LaunchedEffect(Unit) {
        usuariosViewModel.getUsers()
    }

    LaunchedEffect(selectedUserId) {
        if (selectedUserId.isNullOrBlank()) {
            viewModel.loadGlobal()
        } else {
            viewModel.loadForUser(selectedUserId!!)
        }
    }

    val title = if (selectedUserId.isNullOrBlank()) "Permisos" else "Permisos (usuario)"

    fun refreshData() {
        if (selectedUserId.isNullOrBlank()) viewModel.loadGlobal() else viewModel.loadForUser(selectedUserId!!)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = title,
            onBack = { navController.popBackStack() },
            endIcon = null,
            endIconContentDescription = null,
            onRefresh = null,
        )

        PullToRefreshContainer(
            refreshing = uiState is PermisosViewModel.UiState.Loading,
            onRefresh = { refreshData() },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val s = uiState) {
                PermisosViewModel.UiState.Idle,
                PermisosViewModel.UiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Text("Cargando permisos...")
                    }
                }

                is PermisosViewModel.UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Error: ${s.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(onClick = { refreshData() }) { Text("Reintentar") }
                    }
                }

                is PermisosViewModel.UiState.Ok -> {
                    val data = s.data
                    val modules = draft.keys.sorted()

                    Column(modifier = Modifier.fillMaxSize()) {
                        ExposedDropdownMenuBox(
                            expanded = roleExpanded,
                            onExpandedChange = { roleExpanded = !roleExpanded },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedRole,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Rol de usuario") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = roleExpanded,
                                onDismissRequest = { roleExpanded = false }
                            ) {
                                roles.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role) },
                                        onClick = {
                                            selectedRole = role
                                            roleExpanded = false
                                            selectedUserId = null
                                            // "Todos" solo filtra la lista de usuarios; para editar
                                            // en modo Global se necesita un rol concreto.
                                            viewModel.setRole(
                                                if (role == "Todos") PermisosViewModel.DEFAULT_ROLE else role
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = userExpanded,
                            onExpandedChange = { userExpanded = !userExpanded },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedUserName ?: "Global (sin usuario)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Usuario / Rol") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userExpanded) },
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = userExpanded,
                                onDismissRequest = { userExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Global (sin usuario)") },
                                    onClick = {
                                        selectedUserId = null
                                        userExpanded = false
                                    }
                                )
                                filteredUsers.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text("${opt.name} (${opt.role})") },
                                        onClick = {
                                            selectedUserId = opt.id
                                            userExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (selectedUserId.isNullOrBlank()) {
                                        viewModel.saveGlobal()
                                    } else {
                                        viewModel.saveForUser(selectedUserId!!)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Guardar")
                            }
                            OutlinedButton(
                                onClick = {
                                    if (selectedUserId.isNullOrBlank()) {
                                        viewModel.resetGlobal()
                                    } else {
                                        viewModel.resetForUser(selectedUserId!!)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset")
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                if (selectedUserId.isNullOrBlank()) {
                                    val editingRole = if (selectedRole == "Todos") PermisosViewModel.DEFAULT_ROLE else selectedRole
                                    Text(
                                        text = "Editando permisos del rol: $editingRole",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(Modifier.height(6.dp))
                                } else {
                                    Text(
                                        text = "Usuario: ${selectedUserName ?: selectedUserId}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Rol efectivo: ${data.resolvedEffective.role ?: ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(Modifier.height(6.dp))
                                }
                                Text(
                                    text = "Activa/desactiva acciones por modulo (deny-by-default).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            if (modules.isEmpty()) {
                                item {
                                    Text(
                                        text = "No hay modulos disponibles para editar.",
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }

                            items(modules) { moduleKey ->
                                val selected = draft[moduleKey].orEmpty()
                                Card {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = moduleKey,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                        )

                                        ACTIONS.forEach { action ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(actionLabel(action))
                                                Switch(
                                                    checked = selected.contains(action),
                                                    onCheckedChange = { viewModel.toggleAction(moduleKey, action) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

