package com.example.inventappluis370.ui.reportes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun CreateReporteScreen(
    navController: NavController,
    viewModel: ReportesViewModel = hiltViewModel()
) {
    var tipoReporte by remember { mutableStateOf("") }
    var parametros by remember { mutableStateOf("{}") } // Default a un JSON vacío

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is ReportesUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Generar Nuevo Reporte", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

        OutlinedTextField(value = tipoReporte, onValueChange = { tipoReporte = it }, label = { Text("Tipo de Reporte") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = parametros, onValueChange = { parametros = it }, label = { Text("Parámetros (JSON)") }, modifier = Modifier.fillMaxWidth(), maxLines = 5)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (tipoReporte.isNotBlank()) {
                    viewModel.createReporte(tipoReporte, parametros)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is ReportesUiState.Loading
        ) {
            Text("Generar")
        }
    }
}
