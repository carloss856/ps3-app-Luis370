package com.example.inventappluis370.ui.configuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.mapper.toDomain
import com.example.inventappluis370.data.mapper.toDto
import com.example.inventappluis370.domain.model.NotificationSettings
import com.example.inventappluis370.domain.repository.TokenRepository
import com.example.inventappluis370.domain.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ConfiguracionUiState {
    object Loading : ConfiguracionUiState()
    data class Success(
        val settings: NotificationSettings,
        val readOnly: Boolean = false,
        val message: String? = null,
    ) : ConfiguracionUiState()

    data class Error(val message: String) : ConfiguracionUiState()
    object OperationSuccess : ConfiguracionUiState()
}

@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val tokenRepository: TokenRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConfiguracionUiState>(ConfiguracionUiState.Loading)
    val uiState: StateFlow<ConfiguracionUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun defaultSettings(): NotificationSettings = NotificationSettings(
        recibirNotificaciones = true,
        tiposNotificacion = emptyList(),
    )

    private suspend fun ensureUserId(): String? {
        // Contrato actual: /usuarios/{id}/notificaciones usa {id} = id_persona (USR-...).
        return tokenRepository.getUserId()
    }

    fun loadSettings() {
        viewModelScope.launch {
            val userId = ensureUserId()
            if (userId.isNullOrBlank()) {
                _uiState.value = ConfiguracionUiState.Success(
                    settings = defaultSettings(),
                    readOnly = true,
                    message = "No se pudo cargar la configuración: falta id_persona del usuario en la sesión. " +
                        "Verifica que /login devuelva usuario.id_persona (ej: USR-XXXX)."
                )
                return@launch
            }

            _uiState.value = ConfiguracionUiState.Loading
            usuarioRepository.getNotificationSettings(userId)
                .onSuccess { dto ->
                    _uiState.value = ConfiguracionUiState.Success(dto.toDomain())
                }
                .onFailure { e ->
                    _uiState.value = ConfiguracionUiState.Error(e.message ?: "Error al cargar configuración")
                }
        }
    }

    fun saveSettings(settings: NotificationSettings) {
        viewModelScope.launch {
            val userId = ensureUserId()
            if (userId.isNullOrBlank()) {
                _uiState.value = ConfiguracionUiState.Error(
                    "No se pudo guardar la configuración: falta id_persona del usuario (verifica /login)."
                )
                return@launch
            }

            usuarioRepository.saveNotificationSettings(userId, settings.toDto())
                .onSuccess {
                    _uiState.value = ConfiguracionUiState.OperationSuccess
                }
                .onFailure { e ->
                    _uiState.value = ConfiguracionUiState.Error(e.message ?: "Error al guardar configuración")
                }
        }
    }
}
