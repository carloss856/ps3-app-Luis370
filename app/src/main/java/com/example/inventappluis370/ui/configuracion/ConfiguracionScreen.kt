package com.example.inventappluis370.ui.configuracion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.domain.model.NotificationSettings
import com.example.inventappluis370.ui.common.ModuleTopBar

private val CANONICAL_NOTIFICATION_TYPES = listOf(
    "servicios",
    "repuestos",
    "solicitudes_repuesto",
    "equipos",
    "empresa",
    "inventario",
    "reportes",
    "usuarios",
    "notificaciones",
    "garantias",
)

private val NOTIFICATION_TYPE_LABELS = mapOf(
    "servicios" to "Servicios",
    "repuestos" to "Repuestos",
    "solicitudes_repuesto" to "Solicitudes de repuesto",
    "equipos" to "Equipos",
    "empresa" to "Empresas",
    "inventario" to "Inventario",
    "reportes" to "Reportes",
    "usuarios" to "Usuarios",
    "notificaciones" to "Notificaciones",
    "garantias" to "Garantias",
)

private fun notificationTypeLabel(type: String): String =
    NOTIFICATION_TYPE_LABELS[type] ?: type

@Composable
fun ConfiguracionScreen(
    navController: NavController,
    viewModel: ConfiguracionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Configuracion",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Tune,
                endIconContentDescription = "Configuracion"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (val state = uiState) {
                    is ConfiguracionUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is ConfiguracionUiState.Success -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            state.message?.let {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            SettingsForm(
                                settings = state.settings,
                                currentUserId = state.currentUserId,
                                currentUserName = state.currentUserName,
                                readOnly = state.readOnly,
                                onOpenProfile = { id -> navController.navigate("usuarios/$id") },
                                onSave = { viewModel.saveSettings(it) },
                            )
                        }
                    }

                    is ConfiguracionUiState.OperationSuccess -> {
                        LaunchedEffect(Unit) { viewModel.loadSettings() }
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is ConfiguracionUiState.Error -> {
                        Text(
                            state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsForm(
    settings: NotificationSettings,
    currentUserId: String?,
    currentUserName: String?,
    readOnly: Boolean = false,
    onOpenProfile: (String) -> Unit,
    onSave: (NotificationSettings) -> Unit,
) {
    var recibirNotificaciones by remember { mutableStateOf(settings.recibirNotificaciones) }
    val tiposSeleccionados = remember {
        mutableStateListOf<String>().apply {
            addAll(settings.tiposNotificacion)
        }
    }

    LaunchedEffect(settings) {
        recibirNotificaciones = settings.recibirNotificaciones
        tiposSeleccionados.clear()
        tiposSeleccionados.addAll(settings.tiposNotificacion)
    }

    val tiposExtra = remember(tiposSeleccionados) {
        tiposSeleccionados.filter { it !in CANONICAL_NOTIFICATION_TYPES }.distinct().sorted()
    }
    val tiposUi = remember(tiposExtra) { CANONICAL_NOTIFICATION_TYPES + tiposExtra }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Perfil del usuario", style = MaterialTheme.typography.titleMedium)
        Text(
            "Nombre: ${currentUserName ?: "No disponible"}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "ID usuario: ${currentUserId ?: "No disponible"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = {
                if (!currentUserId.isNullOrBlank()) onOpenProfile(currentUserId)
            },
            enabled = !currentUserId.isNullOrBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Editar mi perfil")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Configuracion de Notificaciones", style = MaterialTheme.typography.titleMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recibir notificaciones", modifier = Modifier.weight(1f))
            Switch(
                checked = recibirNotificaciones,
                onCheckedChange = { recibirNotificaciones = it },
                enabled = !readOnly,
            )
        }

        if (recibirNotificaciones) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tipos de notificacion:")

            tiposUi.forEach { tipo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tipo in tiposSeleccionados,
                        onCheckedChange = { checked ->
                            if (readOnly) return@Checkbox
                            if (checked) {
                                if (tipo !in tiposSeleccionados) tiposSeleccionados.add(tipo)
                            } else {
                                tiposSeleccionados.remove(tipo)
                            }
                        },
                        enabled = !readOnly
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(notificationTypeLabel(tipo))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val newSettings = NotificationSettings(
                    recibirNotificaciones = recibirNotificaciones,
                    tiposNotificacion =
                        if (recibirNotificaciones) tiposSeleccionados.distinct().sorted() else emptyList()
                )
                onSave(newSettings)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !readOnly,
        ) {
            Text("Guardar cambios")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

