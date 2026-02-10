package com.example.inventappluis370.ui.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.Notificacion
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.NotificacionRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificacionesUiState {
    object Loading : NotificacionesUiState()
    data class Success(val notificaciones: List<Notificacion>) : NotificacionesUiState()
    data class Error(val message: String) : NotificacionesUiState()
}

@HiltViewModel
class NotificacionesViewModel @Inject constructor(
    private val notificacionRepository: NotificacionRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificacionesUiState>(NotificacionesUiState.Loading)
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    init {
        getNotificaciones()
    }

    fun getNotificaciones() {
        viewModelScope.launch {
            _uiState.value = NotificacionesUiState.Loading
            notificacionRepository.getNotificaciones()
                .onSuccess { notificaciones ->
                    _uiState.value = NotificacionesUiState.Success(notificaciones)
                }
                .onFailure { error ->
                    _uiState.value = NotificacionesUiState.Error(error.message ?: "Error")
                }
        }
    }

    fun deleteNotificacion(id: String) {
        viewModelScope.launch {
            // Optimista: quita de la lista actual si ya está cargada
            val prev = _uiState.value
            if (prev is NotificacionesUiState.Success) {
                _uiState.value = prev.copy(notificaciones = prev.notificaciones.filterNot { it.idNotificacion == id })
            }

            notificacionRepository.deleteNotificacion(id)
                .onSuccess {
                    // No recargamos: ya actualizamos localmente.
                }
                .onFailure { error ->
                    // Si falló, mejor recargar para volver al estado real.
                    getNotificaciones()
                    _uiState.value = NotificacionesUiState.Error(error.message ?: "Error")
                }
        }
    }

    fun setLeida(id: String) {
        viewModelScope.launch {
            val prev = _uiState.value
            if (prev is NotificacionesUiState.Success) {
                _uiState.value = prev.copy(
                    notificaciones = prev.notificaciones.map {
                        if (it.idNotificacion == id) it.copy(leida = true) else it
                    }
                )
            }

            notificacionRepository.setLeida(id)
                .onSuccess {
                    // No recargamos: UI ya refleja el cambio.
                }
                .onFailure { error ->
                    getNotificaciones()
                    _uiState.value = NotificacionesUiState.Error(error.message ?: "Error")
                }
        }
    }

    fun marcarTodasLeidas() {
        viewModelScope.launch {
            val prev = _uiState.value
            if (prev is NotificacionesUiState.Success) {
                _uiState.value = prev.copy(notificaciones = prev.notificaciones.map { it.copy(leida = true) })
            }

            notificacionRepository.marcarTodasLeidas()
                .onSuccess {
                    // No recargamos.
                }
                .onFailure { error ->
                    getNotificaciones()
                    _uiState.value = NotificacionesUiState.Error(error.message ?: "Error")
                }
        }
    }

    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "Notificaciones")
}
