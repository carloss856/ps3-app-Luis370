package com.example.inventappluis370.ui.solicitudes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.data.model.CreateSolicitudRequest

@Composable
fun CreateSolicitudScreen(
    navController: NavController,
    viewModel: SolicitudesRepuestoViewModel = hiltViewModel()
) {
    var repuestoId by remember { mutableStateOf("") }
    var servicioId by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var comentarios by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is SolicitudesUiState.OperationSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Nueva Solicitud de Repuesto", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(value = repuestoId, onValueChange = { repuestoId = it }, label = { Text("ID del Repuesto") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = servicioId, onValueChange = { servicioId = it }, label = { Text("ID del Servicio") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad Solicitada") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = estado, onValueChange = { estado = it }, label = { Text("Estado (Opcional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = comentarios, onValueChange = { comentarios = it }, label = { Text("Comentarios (Opcional)") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val cantidadInt = cantidad.toIntOrNull() ?: 0
                if (repuestoId.isNotBlank() && servicioId.isNotBlank() && cantidadInt > 0) {
                    // El ID de usuario se gestiona en el ViewModel
                    val request = CreateSolicitudRequest(
                        repuestoId = repuestoId,
                        servicioId = servicioId,
                        cantidadSolicitada = cantidadInt,
                        idUsuario = "", // Se rellena en el VM
                        estadoSolicitud = estado.ifBlank { null },
                        comentarios = comentarios.ifBlank { null }
                    )
                    viewModel.createSolicitud(request)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is SolicitudesUiState.Loading
        ) {
            if (uiState is SolicitudesUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Crear Solicitud")
            }
        }
    }
}
