package com.example.inventappluis370.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun PasswordResetScreen(
    navController: NavController,
    viewModel: PasswordResetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableStateOf(1) }

    var email by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    // Reaccionar a los cambios de estado del ViewModel para avanzar al siguiente paso
    LaunchedEffect(uiState) {
        when (uiState) {
            is PasswordResetState.EmailSent -> currentStep = 2
            is PasswordResetState.TokenVerified -> currentStep = 3
            is PasswordResetState.PasswordResetSuccess -> {
                // Regresar al login tras un reseteo exitoso
                navController.popBackStack()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentStep) {
            1 -> Step1_RequestEmail(email = email, onEmailChange = { email = it }, onSend = { viewModel.forgotPassword(email) }, uiState = uiState)
            2 -> Step2_VerifyToken(token = token, onTokenChange = { token = it }, onVerify = { viewModel.verifyToken(email, token) }, uiState = uiState)
            3 -> Step3_ResetPassword(newPassword = newPassword, onPasswordChange = { newPassword = it }, onReset = { viewModel.resetPassword(email, token, newPassword) }, uiState = uiState)
        }
    }
}

@Composable
private fun Step1_RequestEmail(email: String, onEmailChange: (String) -> Unit, onSend: () -> Unit, uiState: PasswordResetState) {
    Text("Recuperar Contraseña")
    OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Introduce tu email") })
    Button(onClick = onSend, enabled = uiState !is PasswordResetState.Loading) { Text("Enviar Código") }
    if (uiState is PasswordResetState.Loading) CircularProgressIndicator()
    if (uiState is PasswordResetState.Error) Text((uiState as PasswordResetState.Error).message, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
}

@Composable
private fun Step2_VerifyToken(token: String, onTokenChange: (String) -> Unit, onVerify: () -> Unit, uiState: PasswordResetState) {
    Text("Verificar Código")
    OutlinedTextField(value = token, onValueChange = onTokenChange, label = { Text("Introduce el código recibido") })
    Button(onClick = onVerify, enabled = uiState !is PasswordResetState.Loading) { Text("Verificar") }
    if (uiState is PasswordResetState.Loading) CircularProgressIndicator()
    if (uiState is PasswordResetState.Error) Text((uiState as PasswordResetState.Error).message, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
}

@Composable
private fun Step3_ResetPassword(newPassword: String, onPasswordChange: (String) -> Unit, onReset: () -> Unit, uiState: PasswordResetState) {
    Text("Nueva Contraseña")
    OutlinedTextField(value = newPassword, onValueChange = onPasswordChange, label = { Text("Introduce tu nueva contraseña") }, visualTransformation = PasswordVisualTransformation())
    Button(onClick = onReset, enabled = uiState !is PasswordResetState.Loading) { Text("Guardar Contraseña") }
    if (uiState is PasswordResetState.Loading) CircularProgressIndicator()
    if (uiState is PasswordResetState.Error) Text((uiState as PasswordResetState.Error).message, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
}

