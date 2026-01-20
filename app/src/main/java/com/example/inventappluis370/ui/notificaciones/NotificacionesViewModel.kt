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
                .onSuccess { notificaciones -> _uiState.value = NotificacionesUiState.Success(notificaciones) }
                .onFailure { _uiState.value = NotificacionesUiState.Error(it.message ?: "Error") }
        }
    }

    fun deleteNotificacion(id: String) {
        viewModelScope.launch {
            notificacionRepository.deleteNotificacion(id)
                .onSuccess { getNotificaciones() }
                .onFailure { _uiState.value = NotificacionesUiState.Error(it.message ?: "Error") }
        }
    }

    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "Notificaciones")
}
