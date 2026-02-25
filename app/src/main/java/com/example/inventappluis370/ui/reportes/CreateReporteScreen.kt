package com.example.inventappluis370.ui.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.ReporteParametros
import com.example.inventappluis370.ui.common.ModuleTopBar

private data class ModuleOption(val key: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateReporteScreen(
    navController: NavController,
    viewModel: ReportesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is ReportesUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.previousBackStackEntry?.savedStateHandle?.set("operation_message", "Reporte generado correctamente")
            navController.popBackStack()
        }
    }

    val formatoOptions = listOf("export_excel" to "Excel", "export_pdf" to "PDF")

    val moduleOptions = listOf(
        ModuleOption("empresas", "Empresas"),
        ModuleOption("usuarios", "Usuarios"),
        ModuleOption("equipos", "Equipos"),
        ModuleOption("propiedad-equipos", "Asignaciones"),
        ModuleOption("servicios", "Servicios"),
        ModuleOption("garantias", "Garantias"),
        ModuleOption("repuestos", "Repuestos"),
        ModuleOption("inventario", "Inventario"),
        ModuleOption("solicitud-repuestos", "Solicitudes de repuestos"),
        ModuleOption("notificaciones", "Notificaciones"),
        ModuleOption("reportes", "Reportes"),
        ModuleOption("rma", "RMA"),
        ModuleOption("tarifas-servicio", "Tarifas"),
    )

    var formatoExpanded by remember { mutableStateOf(false) }
    var formatoKey by remember { mutableStateOf(formatoOptions.first().first) }
    var selectedModules by remember { mutableStateOf(setOf<String>()) }
    var emailDestino by remember { mutableStateOf("") }

    val formatoLabel = formatoOptions.firstOrNull { it.first == formatoKey }?.second ?: "PDF"

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Generar Reporte",
                onBack = { navController.popBackStack() },
                onRefresh = null,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Formato de exportacion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            ExposedDropdownMenuBox(
                expanded = formatoExpanded,
                onExpandedChange = { formatoExpanded = !formatoExpanded },
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = formatoLabel,
                    onValueChange = {},
                    label = { Text("Formato") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatoExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                DropdownMenu(
                    expanded = formatoExpanded,
                    onDismissRequest = { formatoExpanded = false }
                ) {
                    formatoOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.second) },
                            onClick = {
                                formatoKey = opt.first
                                formatoExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = emailDestino,
                onValueChange = { emailDestino = it },
                label = { Text("Enviar a correo (opcional)") },
                placeholder = { Text("correo@dominio.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Modulos incluidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                moduleOptions.forEach { opt ->
                    val selected = selectedModules.contains(opt.key)
                    AssistChip(
                        onClick = {
                            selectedModules = if (selected) selectedModules - opt.key else selectedModules + opt.key
                        },
                        label = { Text(opt.label) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                        )
                    )
                }
            }

            val selectedLabels = moduleOptions.filter { selectedModules.contains(it.key) }.map { it.label }
            Text(
                text = if (selectedLabels.isEmpty()) "Sin modulos seleccionados" else "Incluye: ${selectedLabels.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val enabled = uiState !is ReportesUiState.Loading && selectedModules.isNotEmpty()
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
                        val filtros = mutableMapOf<String, Any?>()
                        val email = emailDestino.trim()
                        if (email.isNotBlank()) filtros["email_destinatario"] = email

                        val parametros = ReporteParametros(
                            modules = selectedModules.toList().sorted(),
                            filters = filtros,
                            source = "android",
                        )
                        viewModel.createReporte(tipoReporte = formatoKey, parametros = parametros)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = enabled
                ) {
                    if (uiState is ReportesUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Generar")
                }
            }
        }
    }
}



