package com.example.inventappluis370.ui.solicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.CreateSolicitudRequest
import com.example.inventappluis370.data.model.SolicitudRepuesto
import com.example.inventappluis370.data.model.UpdateSolicitudRequest
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.SolicitudRepuestoRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SolicitudesUiState {
    object Loading : SolicitudesUiState()
    data class Success(val solicitudes: List<SolicitudRepuesto>) : SolicitudesUiState()
    data class Error(val message: String) : SolicitudesUiState()
    object OperationSuccess : SolicitudesUiState()
}

@HiltViewModel
class SolicitudesRepuestoViewModel @Inject constructor(
    private val repository: SolicitudRepuestoRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SolicitudesUiState>(SolicitudesUiState.Loading)
    val uiState: StateFlow<SolicitudesUiState> = _uiState.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    init {
        getSolicitudes()
    }

    fun getSolicitudes() {
        viewModelScope.launch {
            _uiState.value = SolicitudesUiState.Loading
            repository.getSolicitudes()
                .onSuccess { _uiState.value = SolicitudesUiState.Success(it) }
                .onFailure { _uiState.value = SolicitudesUiState.Error(it.message ?: "Error") }
        }
    }

    fun createSolicitud(request: CreateSolicitudRequest) {
        viewModelScope.launch {
            _uiState.value = SolicitudesUiState.Loading
            repository.createSolicitud(request)
                .onSuccess { _uiState.value = SolicitudesUiState.OperationSuccess }
                .onFailure { _uiState.value = SolicitudesUiState.Error(it.message ?: "Error al crear la solicitud") }
        }
    }

    fun updateSolicitud(id: String, request: UpdateSolicitudRequest) {
        viewModelScope.launch {
            _uiState.value = SolicitudesUiState.Loading
            repository.updateSolicitud(id, request)
                .onSuccess { _uiState.value = SolicitudesUiState.OperationSuccess }
                .onFailure { _uiState.value = SolicitudesUiState.Error(it.message ?: "Error al actualizar la solicitud") }
        }
    }

    fun deleteSolicitud(id: String) {
        viewModelScope.launch {
            repository.deleteSolicitud(id)
                .onSuccess { getSolicitudes() }
                .onFailure { _uiState.value = SolicitudesUiState.Error(it.message ?: "Error al eliminar") }
        }
    }

    fun canCreate(): Boolean = PermissionManager.canCreate(userRole, "SolicitudRepuestos")
    fun canUpdate(): Boolean = PermissionManager.canUpdate(userRole, "SolicitudRepuestos")
    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "SolicitudRepuestos")
}
