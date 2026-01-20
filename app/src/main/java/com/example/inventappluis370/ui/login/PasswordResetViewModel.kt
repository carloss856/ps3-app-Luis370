package com.example.inventappluis370.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Define los posibles estados de la UI para el flujo de reseteo de contraseña.
 */
sealed class PasswordResetState {
    object Idle : PasswordResetState() // Estado inicial
    object Loading : PasswordResetState() // Operación en curso
    object EmailSent : PasswordResetState() // Paso 1 exitoso
    object TokenVerified : PasswordResetState() // Paso 2 exitoso
    object PasswordResetSuccess : PasswordResetState() // Paso 3 exitoso
    data class Error(val message: String) : PasswordResetState()
}

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val uiState: StateFlow<PasswordResetState> = _uiState.asStateFlow()

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = PasswordResetState.Loading
            authRepository.forgotPassword(email)
                .onSuccess { _uiState.value = PasswordResetState.EmailSent }
                .onFailure { _uiState.value = PasswordResetState.Error(it.message ?: "Error") }
        }
    }

    fun verifyToken(email: String, token: String) {
        viewModelScope.launch {
            _uiState.value = PasswordResetState.Loading
            authRepository.verifyToken(email, token)
                .onSuccess { _uiState.value = PasswordResetState.TokenVerified }
                .onFailure { _uiState.value = PasswordResetState.Error(it.message ?: "Error") }
        }
    }

    fun resetPassword(email: String, token: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.value = PasswordResetState.Loading
            authRepository.resetPassword(email, token, contrasena)
                .onSuccess { _uiState.value = PasswordResetState.PasswordResetSuccess }
                .onFailure { _uiState.value = PasswordResetState.Error(it.message ?: "Error") }
        }
    }
    
    fun resetState(){
        _uiState.value = PasswordResetState.Idle
    }
}
