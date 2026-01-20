package com.example.inventappluis370.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.BuildConfig
import com.example.inventappluis370.core.debug.EndpointProbe
import com.example.inventappluis370.data.remote.AuthApiService
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthApiEntryPoint {
    fun authApiService(): AuthApiService
}

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.uiState.collectAsState()

    // --- Debug: probe endpoints ---
    var probeResults by remember { mutableStateOf<List<EndpointProbe.ProbeResult>>(emptyList()) }
    var probing by remember { mutableStateOf(false) }

    // Este efecto se ejecutará de forma segura cuando el estado del login cambie a Success.
    LaunchedEffect(loginState) {
        if (loginState is LoginUiState.Success) {
            // Navega al dashboard y limpia la pantalla de login del historial.
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = loginState !is LoginUiState.Loading
        ) {
            Text("Login")
        }

        // Botón de diagnóstico: solo en DEBUG.
        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    probing = true
                    probeResults = emptyList()
                },
                enabled = !probing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (probing) "Probando endpoint…" else "Diagnóstico API (/login)")
            }

            if (probing) {
                val context = androidx.compose.ui.platform.LocalContext.current
                LaunchedEffect(Unit) {
                    val appContext = context.applicationContext
                    val entryPoint = EntryPointAccessors.fromApplication(appContext, AuthApiEntryPoint::class.java)
                    val authApi = entryPoint.authApiService()

                    probeResults = EndpointProbe.probeAuth(authApi, email = email, password = password)
                    probing = false
                }
            }

            if (probeResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Resultados diagnóstico:")
                Spacer(modifier = Modifier.height(8.dp))
                probeResults.forEach { r ->
                    val details = buildString {
                        append("• ${r.name} => ok=${r.ok}, status=${r.status}, headerExp=${r.tokenExpiresAtHeader ?: "(null)"}\n")
                        r.rawBody?.let { append("  body: $it\n") }
                        r.rawErrorBody?.let { append("  errorBody: $it\n") }
                        if (r.rawBody == null && r.rawErrorBody == null) {
                            append("  msg: ${r.message}\n")
                        }
                    }
                    Text(
                        text = details.trimEnd(),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿Olvidaste tu contraseña?",
            modifier = Modifier.clickable { navController.navigate("password-reset") },
            color = MaterialTheme.colorScheme.primary
        )

        if (loginState is LoginUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        if (loginState is LoginUiState.Error) {
            val errorMessage = (loginState as LoginUiState.Error).message
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
