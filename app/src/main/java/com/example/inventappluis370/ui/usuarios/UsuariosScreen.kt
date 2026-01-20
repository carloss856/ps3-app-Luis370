package com.example.inventappluis370.ui.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun UsuariosScreen(
    navController: NavController,
    viewModel: UsuariosViewModel = hiltViewModel()
) {
    val users = viewModel.usuariosPaged.collectAsLazyPagingItems()

    val refreshing = users.loadState.refresh is LoadState.Loading

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
                    Icon(Icons.Default.Add, contentDescription = "Añadir Usuario")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
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
                                        // Solo usar id_persona como id principal.
                                        if (!userIdPersona.isNullOrBlank()) viewModel.deleteUser(userIdPersona)
                                    },
                                    onEdit = {
                                        if (!userIdPersona.isNullOrBlank()) navController.navigate("usuarios/$userIdPersona")
                                    },
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
                                        text = "Error cargando más: ${PagingUi.messageOf(error)}",
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
    }
}

@Composable
fun UsuarioItem(
    user: Usuario,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    val displayName = user.nombre ?: "(Sin nombre)"
    val displayEmail = user.email ?: ""
    val displayRol = user.tipo ?: ""

    // Regla del proyecto: el ID principal debe ser id_persona. No mostramos/ni usamos _id como fallback.
    val businessId = user.idPersona

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(displayName, style = MaterialTheme.typography.titleMedium)
                if (displayEmail.isNotBlank()) Text(displayEmail)
                if (displayRol.isNotBlank()) {
                    Text("Rol: $displayRol", style = MaterialTheme.typography.bodySmall)
                }

                if (!businessId.isNullOrBlank()) {
                    Text("ID: $businessId", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        "ERROR: usuario sin id_persona (debe corregirse en backend)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
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
