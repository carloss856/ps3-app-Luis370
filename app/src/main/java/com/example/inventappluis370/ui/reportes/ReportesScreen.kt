package com.example.inventappluis370.ui.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.Reporte
import com.example.inventappluis370.ui.common.ModuleTopBar
import com.example.inventappluis370.ui.common.PullToRefreshContainer

@Composable
fun ReportesScreen(
    navController: NavController,
    viewModel: ReportesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedReporte by remember { mutableStateOf<Reporte?>(null) }
    var emailTarget by remember { mutableStateOf("") }
    var showEmailDialog by remember { mutableStateOf(false) }
    var operationMessage by remember { mutableStateOf<String?>(null) }

    val refresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")
    LaunchedEffect(refresh) {
        if (refresh == true) {
            viewModel.getReportes()
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }

    LaunchedEffect(Unit) {
        val msg = navController.currentBackStackEntry?.savedStateHandle?.get<String>("operation_message")
        if (!msg.isNullOrBlank()) {
            operationMessage = msg
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("operation_message")
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ReportesUiState.OperationSuccess) {
            operationMessage = "Reporte generado correctamente"
            viewModel.getReportes()
        }
    }

    val refreshing = uiState is ReportesUiState.Loading

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Reportes",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.BarChart,
                endIconContentDescription = "Reportes",
                onRefresh = null,
            )
        },
        floatingActionButton = {
            if (viewModel.canCreate()) {
                FloatingActionButton(onClick = { navController.navigate("reportes/new") }) {
                    Icon(Icons.Default.Add, contentDescription = "Generar Reporte")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshContainer(
            refreshing = refreshing,
            onRefresh = { viewModel.getReportes() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ReportesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ReportesUiState.Error -> {
                    Text(
                        "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ReportesUiState.Success -> {
                    val reportes = state.reportes
                    if (reportes.isEmpty()) {
                        Text("No hay reportes para mostrar.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(reportes) { reporte ->
                                ReporteItem(
                                    reporte = reporte,
                                    onOpen = { selectedReporte = reporte },
                                    onDownload = {
                                        viewModel.regenerateReporte(reporte, formato = "export_pdf", destinatarioEmail = null)
                                    },
                                    onSendEmail = {
                                        selectedReporte = reporte
                                        emailTarget = ""
                                        showEmailDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                ReportesUiState.OperationSuccess -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        if (selectedReporte != null && !showEmailDialog) {
            val r = selectedReporte!!
            AlertDialog(
                onDismissRequest = { selectedReporte = null },
                confirmButton = { TextButton(onClick = { selectedReporte = null }) { Text("Cerrar") } },
                title = { Text("Detalle de reporte") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Tipo: ${r.tipoReporte ?: "-"}")
                        Text("Generado por: ${r.idUsuario ?: "-"}")
                        Text("Fecha: ${r.fechaGeneracion ?: "-"}")
                        Text("Parametros: ${r.parametrosUtilizados ?: "-"}")
                    }
                }
            )
        }

        if (showEmailDialog && selectedReporte != null) {
            val rep = selectedReporte!!
            AlertDialog(
                onDismissRequest = { showEmailDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.regenerateReporte(rep, formato = "export_pdf", destinatarioEmail = emailTarget)
                        showEmailDialog = false
                    }) { Text("Enviar") }
                },
                dismissButton = { TextButton(onClick = { showEmailDialog = false }) { Text("Cancelar") } },
                title = { Text("Enviar reporte por correo") },
                text = {
                    OutlinedTextField(
                        value = emailTarget,
                        onValueChange = { emailTarget = it },
                        label = { Text("Correo destino") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
fun ReporteItem(
    reporte: Reporte,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onSendEmail: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reporte.tipoReporte ?: "(Sin tipo)",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(text = "Generado por: ${reporte.idUsuario ?: "(desconocido)"}")
                Text(text = "Fecha: ${reporte.fechaGeneracion ?: "(sin fecha)"}")
                TextButton(onClick = onOpen) { Text("Ver detalle") }
            }
            Row {
                IconButton(onClick = onDownload) {
                    Icon(Icons.Default.Download, contentDescription = "Descargar")
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onSendEmail) {
                    Icon(Icons.Default.Email, contentDescription = "Enviar por correo")
                }
            }
        }
    }
}

