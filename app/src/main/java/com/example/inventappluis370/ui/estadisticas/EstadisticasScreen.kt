package com.example.inventappluis370.ui.estadisticas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.domain.model.*
import com.example.inventappluis370.ui.common.ModuleTopBar

private val moduleTitles = linkedMapOf(
    "empresas" to "Empresas",
    "usuarios" to "Usuarios",
    "equipos" to "Equipos",
    "propiedad-equipos" to "Asignaciones",
    "servicios" to "Servicios",
    "garantias" to "Garantías",
    "repuestos" to "Repuestos",
    "inventario" to "Inventario (entradas)",
    "solicitud-repuestos" to "Solicitudes de repuestos",
    // Requisito: quitar sección de notificaciones recientes de Estadísticas
    // "notificaciones" to "Notificaciones",
    "reportes" to "Reportes",
    "rma" to "RMA",
    "tarifas-servicio" to "Tarifas",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    navController: NavController,
    viewModel: EstadisticasViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val moduleUiState by viewModel.moduleUiState.collectAsState()
    val periodByModule by viewModel.periodByModule.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
        // precargar stats para los módulos principales (si el usuario solo entra a ver, que ya estén listas)
        moduleTitles.keys.forEach { module ->
            viewModel.refreshModule(module)
        }
    }

    Scaffold(
        topBar = {
            ModuleTopBar(
                title = "Estadísticas",
                onBack = { navController.popBackStack() },
                endIcon = Icons.Default.Dashboard,
                endIconContentDescription = "Estadísticas",
                onRefresh = { viewModel.loadDashboard() },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (val s = uiState) {
                EstadisticasViewModel.UiState.Idle,
                EstadisticasViewModel.UiState.Loading -> {
                    item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                }

                is EstadisticasViewModel.UiState.Error -> {
                    item {
                        Text(
                            text = s.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                is EstadisticasViewModel.UiState.Ok -> {
                    val data = s.data
                    item {
                        Text(
                            text = "KPIs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    items(data.cards) { card ->
                        KpiCard(card)
                    }

                    data.lists?.repuestosCriticos?.takeIf { it.isNotEmpty() }?.let { repuestos ->
                        item { SectionTitle("Repuestos críticos") }
                        // Requisito: quitar "(top 10)". No limitamos explícitamente aquí.
                        items(repuestos) { r ->
                            SimpleRowCard(
                                title = r.nombreRepuesto,
                                subtitle = "${r.idRepuesto} · stock=${r.cantidadDisponible ?: "?"} · crítico=${r.nivelCritico ?: "?"}"
                            )
                        }
                    }

                    item { SectionTitle("Estadísticas por módulo") }

                    moduleTitles.forEach { (moduleKey, title) ->
                        item {
                            val period = periodByModule[moduleKey] ?: StatsPeriod.MONTH
                            val mState = moduleUiState[moduleKey] ?: EstadisticasViewModel.ModuleUiState.Idle
                            ModuleStatsCard(
                                title = title,
                                period = period,
                                state = mState,
                                onPeriodChange = { viewModel.setPeriod(moduleKey, it) },
                                onRetry = { viewModel.refreshModule(moduleKey) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun KpiCard(card: DashboardKpiCard) {
    var showInfo by remember { mutableStateOf(false) }

    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(card.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { showInfo = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Info")
                }
            }

            DashboardValue(card.value)
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("Cerrar") }
            },
            title = { Text(card.title) },
            text = { Text(kpiDescription(card.key)) },
        )
    }
}

private fun kpiDescription(key: String): String {
    // Descripciones cortas (no vienen del backend). Se puede ampliar luego.
    return when (key) {
        "equipos.total" -> "Cantidad total de equipos visibles para tu usuario según tus permisos."
        "repuestos.criticos" -> "Cantidad de repuestos con stock igual o por debajo del nivel crítico."
        "solicitudes.pendientes" -> "Solicitudes de repuestos en estado Pendiente."
        "servicios.pendientes" -> "Servicios en estado Pendiente."
        "servicios.en_proceso" -> "Servicios en estado En proceso."
        "servicios.finalizados" -> "Servicios en estado Finalizado."
        "notificaciones.recientes" -> "Notificaciones más recientes registradas para tu usuario."
        else -> "Indicador del sistema (${key})."
    }
}

@Composable
private fun DashboardValue(value: Any?) {
    when (value) {
        null -> Text("-")
        is Number -> {
            val asLong = value.toLong()
            val rendered = if (kotlin.math.abs(value.toDouble() - asLong.toDouble()) < 0.00001) {
                asLong.toString()
            } else {
                // si viene con decimales, igual mostramos entero (requisito)
                asLong.toString()
            }
            Text(rendered, style = MaterialTheme.typography.headlineSmall)
        }
        is String, is Boolean -> Text(value.toString(), style = MaterialTheme.typography.headlineSmall)
        is Map<*, *> -> {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                value.entries.forEach { (k, v) ->
                    Text("${k ?: ""}: ${v ?: ""}")
                }
            }
        }
        is List<*> -> {
            Text(value.joinToString(prefix = "[", postfix = "]") { it?.toString().orEmpty() })
        }
        else -> Text(value.toString())
    }
}

@Composable
private fun SimpleRowCard(title: String, subtitle: String? = null) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Medium)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ModuleStatsCard(
    title: String,
    period: StatsPeriod,
    state: EstadisticasViewModel.ModuleUiState,
    onPeriodChange: (StatsPeriod) -> Unit,
    onRetry: () -> Unit,
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                PeriodSelector(selected = period, onSelected = onPeriodChange)
            }

            when (state) {
                EstadisticasViewModel.ModuleUiState.Idle,
                EstadisticasViewModel.ModuleUiState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                is EstadisticasViewModel.ModuleUiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onRetry) { Text("Reintentar") }
                }

                is EstadisticasViewModel.ModuleUiState.Ok -> {
                    // Requisito: quitar el subtítulo/extra debajo del nombre del módulo.
                    // Dejamos una línea simple con el total.
                    Text("Total: ${state.data.total}", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(selected: StatsPeriod, onSelected: (StatsPeriod) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
            Text(selected.label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            StatsPeriod.entries.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p.label) },
                    onClick = {
                        expanded = false
                        onSelected(p)
                    }
                )
            }
        }
    }
}

@Composable
private fun MiniBars(buckets: List<StatsBucket>) {
    if (buckets.isEmpty()) {
        Text("Sin datos", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val max = buckets.maxOf { it.count }.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        buckets.take(12).forEach { b ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = b.label,
                    modifier = Modifier.width(64.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val fraction = b.count.toFloat() / max.toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(b.count.toString(), style = MaterialTheme.typography.bodySmall)
            }
        }
        if (buckets.size > 12) {
            Text("…", color = Color.Gray)
        }
    }
}
