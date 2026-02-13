package com.example.inventappluis370.ui.configuracion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.inventappluis370.domain.model.NotificationSettings
import com.example.inventappluis370.ui.common.ModuleTopBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.navigation.NavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox

private val CANONICAL_NOTIFICATION_TYPES = listOf(
    "servicios",
    "repuestos",
    "solicitudes_repuesto", // en UI/web se usa con underscore
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
    "garantias" to "Garantías",
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
                title = "Configuración",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Tune,
                endIconContentDescription = "Configuración"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
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
                                readOnly = state.readOnly,
                                onSave = { viewModel.saveSettings(it) },
                            )
                        }
                    }
                    is ConfiguracionUiState.OperationSuccess -> {
                        LaunchedEffect(Unit) { viewModel.loadSettings() }
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is ConfiguracionUiState.Error -> {
                        Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsForm(
    settings: NotificationSettings,
    readOnly: Boolean = false,
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

    // Tipos a mostrar: primero los canónicos; luego los que vengan del backend y no estén listados.
    val tiposExtra = remember(tiposSeleccionados) {
        tiposSeleccionados.filter { it !in CANONICAL_NOTIFICATION_TYPES }.distinct().sorted()
    }
    val tiposUi = remember(tiposExtra) { CANONICAL_NOTIFICATION_TYPES + tiposExtra }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Configuración de Notificaciones", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recibir notificaciones", modifier = Modifier.weight(1f))
            Switch(
                checked = recibirNotificaciones,
                onCheckedChange = { recibirNotificaciones = it },
                enabled = !readOnly,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Tipos de notificación:")
        Spacer(modifier = Modifier.height(8.dp))

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
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val newSettings = NotificationSettings(
                    recibirNotificaciones = recibirNotificaciones,
                    tiposNotificacion = tiposSeleccionados.distinct().sorted()
                )
                onSave(newSettings)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !readOnly,
        ) {
            Text("Guardar Cambios")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
