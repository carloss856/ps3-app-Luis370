package com.example.inventappluis370.ui.reportes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
            navController.popBackStack()
        }
    }

    // Nota: en web se usan valores estables para exportaciones: export_excel / export_pdf.
    // Para el módulo "Generar reporte" mantenemos opciones simples y estables.
    val tipoOptions = listOf(
        "general",
        "inventario",
        "servicios",
        "repuestos",
        "equipos",
        "usuarios",
        "empresas",
        "tarifas-servicio",
        "reportes",
        "notificaciones",
        "rma",
        "garantias",
        // Exportaciones (paridad con web)
        "export_excel",
        "export_pdf",
    )

    val moduleOptions = listOf(
        ModuleOption("empresas", "Empresas"),
        ModuleOption("usuarios", "Usuarios"),
        ModuleOption("equipos", "Equipos"),
        ModuleOption("propiedad-equipos", "Asignaciones"),
        ModuleOption("servicios", "Servicios"),
        ModuleOption("garantias", "Garantías"),
        ModuleOption("repuestos", "Repuestos"),
        ModuleOption("inventario", "Inventario"),
        ModuleOption("solicitud-repuestos", "Solicitudes de repuestos"),
        ModuleOption("notificaciones", "Notificaciones"),
        ModuleOption("reportes", "Reportes"),
        ModuleOption("rma", "RMA"),
        ModuleOption("tarifas-servicio", "Tarifas"),
    )

    var expanded by remember { mutableStateOf(false) }
    var tipoReporte by remember { mutableStateOf(tipoOptions.first()) }
    var selectedModules by remember { mutableStateOf(setOf<String>()) }

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tipo de reporte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = tipoReporte,
                    onValueChange = {},
                    label = { Text("Tipo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    tipoOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                tipoReporte = opt
                                expanded = false
                            }
                        )
                    }
                }
            }

            Text("Módulos incluidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            // Chips multi-selección (simple)
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
                        leadingIcon = {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = {
                                    selectedModules = if (it) selectedModules + opt.key else selectedModules - opt.key
                                }
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val enabled = uiState !is ReportesUiState.Loading
            Button(
                onClick = {
                    val parametros = ReporteParametros(
                        modules = selectedModules.toList().sorted(),
                        filters = emptyMap(),
                        source = "android",
                    )
                    viewModel.createReporte(tipoReporte = tipoReporte, parametros = parametros)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) {
                if (!enabled) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text("Generar")
            }
        }
    }
}
