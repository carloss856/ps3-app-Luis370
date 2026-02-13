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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavController
import com.example.inventappluis370.ui.common.ModuleTopBar

private val ACTIONS = listOf("index", "show", "store", "update", "destroy")

private fun actionLabel(action: String): String = when (action.lowercase()) {
    // Requisito: en Android, "show" es equivalente a "index" (ver listados/detalles).
    // En UI ambos deben verse como "Ver".
    "index" -> "Ver"
    "show" -> "Ver"
    "store" -> "Crear"
    "update" -> "Editar"
    "destroy" -> "Eliminar"
    else -> action
}

/**
 * Editor de Permisos (RBAC overrides) equivalente al Web.
 *
 * Implementado:
 * - GET/PUT/RESET global: /api/permissions
 *
 * Pendiente (paridad total): overrides por usuario (/api/permissions/user/{id}).
 */
@Composable
fun PermisosScreen(
    navController: NavController,
    viewModel: PermisosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val draft by viewModel.draftModules.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val userIdArg = remember(backStackEntry) {
        backStackEntry?.arguments?.getString("userId")
    }

    LaunchedEffect(userIdArg) {
        if (userIdArg.isNullOrBlank()) {
            viewModel.loadGlobal()
        } else {
            viewModel.loadForUser(userIdArg)
        }
    }

    val title = if (userIdArg.isNullOrBlank()) "Permisos" else "Permisos (usuario)"

    Column(modifier = Modifier.fillMaxSize()) {
        ModuleTopBar(
            title = title,
            onBack = { navController.popBackStack() },
            endIcon = null,
            endIconContentDescription = null,
            onRefresh = {
                if (userIdArg.isNullOrBlank()) viewModel.loadGlobal() else viewModel.loadForUser(userIdArg)
            },
        )

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
                    Button(onClick = {
                        if (userIdArg.isNullOrBlank()) viewModel.loadGlobal() else viewModel.loadForUser(userIdArg)
                    }) { Text("Reintentar") }
                }
            }

            is PermisosViewModel.UiState.Ok -> {
                val data = s.data
                val modules = draft.keys.sorted()

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (userIdArg.isNullOrBlank()) {
                                    viewModel.saveGlobal()
                                } else {
                                    viewModel.saveForUser(userIdArg)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Guardar")
                        }
                        OutlinedButton(
                            onClick = {
                                if (userIdArg.isNullOrBlank()) {
                                    viewModel.resetGlobal()
                                } else {
                                    viewModel.resetForUser(userIdArg)
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
                            if (userIdArg.isNullOrBlank()) {
                                Text(
                                    text = "Role: ${data.resolvedEffective.role ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.height(6.dp))
                            } else {
                                Text(
                                    text = "Usuario: $userIdArg",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Editando override de permisos del usuario (no cambia la matriz global).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                            Text(
                                text = "Activa/desactiva acciones por módulo (deny-by-default).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (modules.isEmpty()) {
                            item {
                                Text(
                                    text = "No hay módulos disponibles para editar.",
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
                                            androidx.compose.material3.Switch(
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
