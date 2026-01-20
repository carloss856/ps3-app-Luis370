package com.example.inventappluis370.ui.repuestos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.Repuesto
import com.example.inventappluis370.data.model.RepuestoRequest
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.RepuestoRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RepuestosUiState {
    object Loading : RepuestosUiState()
    data class Success(val repuestos: List<Repuesto>) : RepuestosUiState()
    data class Error(val message: String) : RepuestosUiState()
    object OperationSuccess : RepuestosUiState()
}

@HiltViewModel
class RepuestosViewModel @Inject constructor(
    private val repuestoRepository: RepuestoRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RepuestosUiState>(RepuestosUiState.Loading)
    val uiState: StateFlow<RepuestosUiState> = _uiState.asStateFlow()

    private val _selectedRepuesto = MutableStateFlow<Repuesto?>(null)
    val selectedRepuesto: StateFlow<Repuesto?> = _selectedRepuesto.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    init {
        getRepuestos()
    }

    fun getRepuestos() {
        viewModelScope.launch {
            _uiState.value = RepuestosUiState.Loading
            repuestoRepository.getRepuestos()
                .onSuccess { repuestos ->
                    _uiState.value = RepuestosUiState.Success(repuestos)
                }
                .onFailure { error ->
                    _uiState.value = RepuestosUiState.Error(error.message ?: "Ocurrió un error inesperado")
                }
        }
    }

    fun getRepuestoById(id: String) {
        val repuestos = (uiState.value as? RepuestosUiState.Success)?.repuestos
        // Corregido: Usar idRepuesto
        _selectedRepuesto.value = repuestos?.find { it.idRepuesto == id }
    }

    fun createRepuesto(repuestoRequest: RepuestoRequest) {
        viewModelScope.launch {
            _uiState.value = RepuestosUiState.Loading
            repuestoRepository.createRepuesto(repuestoRequest)
                .onSuccess { _uiState.value = RepuestosUiState.OperationSuccess }
                .onFailure { _uiState.value = RepuestosUiState.Error(it.message ?: "Error") }
        }
    }

    fun updateRepuesto(id: String, repuestoRequest: RepuestoRequest) {
        viewModelScope.launch {
            _uiState.value = RepuestosUiState.Loading
            repuestoRepository.updateRepuesto(id, repuestoRequest)
                .onSuccess { _uiState.value = RepuestosUiState.OperationSuccess }
                .onFailure { _uiState.value = RepuestosUiState.Error(it.message ?: "Error") }
        }
    }

    fun deleteRepuesto(id: String) {
        viewModelScope.launch {
            repuestoRepository.deleteRepuesto(id)
                .onSuccess { getRepuestos() } 
                .onFailure { _uiState.value = RepuestosUiState.Error(it.message ?: "Error") }
        }
    }

    fun canCreate(): Boolean = PermissionManager.canCreate(userRole, "Repuestos")
    fun canUpdate(): Boolean = PermissionManager.canUpdate(userRole, "Repuestos")
    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "Repuestos")
}
