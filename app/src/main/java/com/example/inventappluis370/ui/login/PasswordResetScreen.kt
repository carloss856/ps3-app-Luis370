package com.example.inventappluis370.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.ui.common.ThemeToggleButton

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

    LaunchedEffect(uiState) {
        when (uiState) {
            is PasswordResetState.EmailSent -> currentStep = 2
            is PasswordResetState.TokenVerified -> currentStep = 3
            is PasswordResetState.PasswordResetSuccess -> navController.popBackStack()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }
            Spacer(modifier = Modifier.weight(1f))
            ThemeToggleButton()
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(text = "Recuperar Contrasena", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        when (currentStep) {
            1 -> Step1RequestEmail(
                email = email,
                onEmailChange = { email = it },
                onSend = { viewModel.forgotPassword(email) },
                uiState = uiState
            )

            2 -> Step2VerifyToken(
                token = token,
                onTokenChange = { token = it },
                onVerify = { viewModel.verifyToken(email, token) },
                uiState = uiState
            )

            3 -> Step3ResetPassword(
                newPassword = newPassword,
                onPasswordChange = { newPassword = it },
                onReset = { viewModel.resetPassword(email, token, newPassword) },
                uiState = uiState
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun Step1RequestEmail(
    email: String,
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit,
    uiState: PasswordResetState
) {
    Text(
        text = "Paso 1 de 3: enviaremos un codigo a tu correo",
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(onClick = onSend, enabled = uiState !is PasswordResetState.Loading, modifier = Modifier.fillMaxWidth()) {
        Text("Enviar codigo")
    }

    StepFeedback(uiState)
}

@Composable
private fun Step2VerifyToken(
    token: String,
    onTokenChange: (String) -> Unit,
    onVerify: () -> Unit,
    uiState: PasswordResetState
) {
    Text(
        text = "Paso 2 de 3: verifica el codigo recibido",
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = token,
        onValueChange = onTokenChange,
        label = { Text("Codigo") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(onClick = onVerify, enabled = uiState !is PasswordResetState.Loading, modifier = Modifier.fillMaxWidth()) {
        Text("Verificar")
    }

    StepFeedback(uiState)
}

@Composable
private fun Step3ResetPassword(
    newPassword: String,
    onPasswordChange: (String) -> Unit,
    onReset: () -> Unit,
    uiState: PasswordResetState
) {
    Text(
        text = "Paso 3 de 3: define tu nueva contrasena",
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = newPassword,
        onValueChange = onPasswordChange,
        label = { Text("Nueva contrasena") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(onClick = onReset, enabled = uiState !is PasswordResetState.Loading, modifier = Modifier.fillMaxWidth()) {
        Text("Guardar contrasena")
    }

    StepFeedback(uiState)
}

@Composable
private fun StepFeedback(uiState: PasswordResetState) {
    if (uiState is PasswordResetState.Loading) {
        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
    }

    val error = uiState as? PasswordResetState.Error
    if (error != null) {
        Text(
            text = error.message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

