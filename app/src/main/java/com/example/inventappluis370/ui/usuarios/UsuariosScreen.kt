package com.example.inventappluis370.ui.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.inventappluis370.data.model.Usuario
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PagingUi
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun UsuariosScreen(
    navController: NavController,
    viewModel: UsuariosViewModel = hiltViewModel()
) {
    val users = viewModel.usuariosPaged.collectAsLazyPagingItems()
    val refreshing = users.loadState.refresh is LoadState.Loading
    var selectedUser by remember { mutableStateOf<Usuario?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var operationMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val msg = navController.currentBackStackEntry?.savedStateHandle?.get<String>("operation_message")
        if (!msg.isNullOrBlank()) {
            operationMessage = msg
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("operation_message")
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Usuarios",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.People,
                endIconContentDescription = "Usuarios"
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("usuarios/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Anadir Usuario")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshContainer(
            refreshing = refreshing,
            onRefresh = { users.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    refreshing && users.itemCount == 0 -> {
                        CircularProgressIndicator()
                    }

                    users.loadState.refresh is LoadState.Error -> {
                        val error = (users.loadState.refresh as LoadState.Error).error
                        Text(
                            "Error: ${PagingUi.messageOf(error)}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    users.itemCount == 0 -> {
                        Text("No hay usuarios para mostrar.")
                    }

                    else -> {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                count = users.itemCount,
                                key = users.itemKey { it.idPersona ?: "" }
                            ) { index ->
                                val user = users[index] ?: return@items
                                val userIdPersona = user.idPersona

                                UsuarioItem(
                                    user = user,
                                    onDelete = {
                                        if (!userIdPersona.isNullOrBlank()) pendingDeleteId = userIdPersona
                                    },
                                    onEdit = {
                                        if (!userIdPersona.isNullOrBlank()) navController.navigate("usuarios/$userIdPersona")
                                    },
                                    onView = { selectedUser = user },
                                    canUpdate = viewModel.canUpdate() && !userIdPersona.isNullOrBlank(),
                                    canDelete = viewModel.canDelete() && !userIdPersona.isNullOrBlank()
                                )
                            }

                            item {
                                if (users.loadState.append is LoadState.Loading) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                            }

                            item {
                                if (users.loadState.append is LoadState.Error) {
                                    val error = (users.loadState.append as LoadState.Error).error
                                    Text(
                                        text = "Error cargando mas: ${PagingUi.messageOf(error)}",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedUser != null) {
            val u = selectedUser!!
            AlertDialog(
                onDismissRequest = { selectedUser = null },
                confirmButton = { TextButton(onClick = { selectedUser = null }) { Text("Cerrar") } },
                title = { Text("Detalle de usuario") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Nombre: ${u.nombre ?: "-"}")
                        Text("Correo: ${u.email ?: "-"}")
                        Text("Telefono: ${u.telefono ?: "-"}")
                        Text("Rol: ${u.tipo ?: "-"}")
                        Text("Empresa: ${u.idEmpresa ?: "-"}")
                    }
                }
            )
        }

        if (pendingDeleteId != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteId = null },
                confirmButton = {
                    TextButton(onClick = {
                        val id = pendingDeleteId
                        pendingDeleteId = null
                        if (!id.isNullOrBlank()) {
                            viewModel.deleteUser(id)
                            users.refresh()
                            operationMessage = "Usuario eliminado correctamente"
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text("Cancelar") } },
                title = { Text("Confirmar eliminacion") },
                text = { Text("Deseas eliminar este usuario?") }
            )
        }

        if (operationMessage != null) {
            AlertDialog(
                onDismissRequest = { operationMessage = null },
                confirmButton = { TextButton(onClick = { operationMessage = null }) { Text("Aceptar") } },
                title = { Text("Operacion completada") },
                text = { Text(operationMessage!!) }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun UsuarioItem(
    user: Usuario,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    val displayName = user.nombre ?: "(Sin nombre)"
    val displayEmail = user.email ?: ""
    val displayRol = user.tipo ?: ""

    Card(modifier = Modifier.fillMaxWidth(), onClick = onView) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(displayName, style = MaterialTheme.typography.titleMedium)
                if (displayEmail.isNotBlank()) Text(displayEmail)
                if (displayRol.isNotBlank()) {
                    Text("Rol: $displayRol", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (canUpdate) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
            }
            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

