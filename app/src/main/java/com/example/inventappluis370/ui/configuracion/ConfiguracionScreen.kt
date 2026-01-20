package com.example.inventappluis370.ui.configuracion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

    var nuevoTipo by remember { mutableStateOf("") }

    LaunchedEffect(settings) {
        recibirNotificaciones = settings.recibirNotificaciones
        tiposSeleccionados.clear()
        tiposSeleccionados.addAll(settings.tiposNotificacion)
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
        Text("Tipos de notificación (según backend):")

        if (tiposSeleccionados.isEmpty()) {
            Text("(Sin tipos configurados)", style = MaterialTheme.typography.bodySmall)
        } else {
            tiposSeleccionados.forEach { tipo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tipo, modifier = Modifier.weight(1f))
                    Button(onClick = { tiposSeleccionados.remove(tipo) }) {
                        Text("Quitar")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nuevoTipo,
            onValueChange = { nuevoTipo = it },
            label = { Text("Agregar tipo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val t = nuevoTipo.trim()
                if (t.isNotBlank() && t !in tiposSeleccionados) {
                    tiposSeleccionados.add(t)
                    nuevoTipo = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !readOnly && nuevoTipo.trim().isNotBlank()
        ) {
            Text("Agregar")
        }

        Spacer(modifier = Modifier.weight(1.0f))

        Button(
            onClick = {
                val newSettings = NotificationSettings(
                    recibirNotificaciones = recibirNotificaciones,
                    tiposNotificacion = tiposSeleccionados.toList()
                )
                onSave(newSettings)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !readOnly,
        ) {
            Text("Guardar Cambios")
        }
    }
}
