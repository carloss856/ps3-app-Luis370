package com.example.inventappluis370.ui.servicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.CreateParteTrabajoRequest
import com.example.inventappluis370.data.model.ParteTrabajo
import com.example.inventappluis370.data.model.UpdateParteTrabajoRequest
import com.example.inventappluis370.domain.repository.ServicioRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PartesTrabajoUiState {
    object Loading : PartesTrabajoUiState()
    data class Success(
        val partes: List<ParteTrabajo>,
        val costoManoObra: Double,
        val tiempoTotal: Int
    ) : PartesTrabajoUiState()
    data class Error(val message: String) : PartesTrabajoUiState()
    object OperationSuccess : PartesTrabajoUiState()
}

@HiltViewModel
class PartesTrabajoViewModel @Inject constructor(
    private val servicioRepository: ServicioRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PartesTrabajoUiState>(PartesTrabajoUiState.Loading)
    val uiState: StateFlow<PartesTrabajoUiState> = _uiState.asStateFlow()

    private val _selectedParte = MutableStateFlow<ParteTrabajo?>(null)
    val selectedParte: StateFlow<ParteTrabajo?> = _selectedParte.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, List<String>>> = _fieldErrors.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    fun getPartes(servicioId: String) {
        viewModelScope.launch {
            _uiState.value = PartesTrabajoUiState.Loading
            servicioRepository.getPartes(servicioId)
                .onSuccess { response -> 
                    _uiState.value = PartesTrabajoUiState.Success(
                        partes = response.partes,
                        costoManoObra = response.costoManoObra,
                        tiempoTotal = response.tiempoTotalMinutos
                    ) 
                }
                .onFailure { _uiState.value = PartesTrabajoUiState.Error(it.message ?: "Error") }
        }
    }

    fun getParteById(servicioId: String, parteId: String) {
        val currentPartes = (uiState.value as? PartesTrabajoUiState.Success)?.partes
        _selectedParte.value = currentPartes?.find { it.idParte == parteId }
    }

    fun createParte(servicioId: String, request: CreateParteTrabajoRequest) {
        viewModelScope.launch {
            _uiState.value = PartesTrabajoUiState.Loading
            servicioRepository.createParte(servicioId, request)
                .onSuccess { 
                    _uiState.value = PartesTrabajoUiState.OperationSuccess 
                }
                .onFailure { 
                    _uiState.value = PartesTrabajoUiState.Error(it.message ?: "Error") 
                }
        }
    }

    fun updateParte(servicioId: String, parteId: String, request: UpdateParteTrabajoRequest) {
        viewModelScope.launch {
            _uiState.value = PartesTrabajoUiState.Loading
            servicioRepository.updateParte(servicioId, parteId, request)
                .onSuccess { 
                    _uiState.value = PartesTrabajoUiState.OperationSuccess 
                }
                .onFailure { 
                    _uiState.value = PartesTrabajoUiState.Error(it.message ?: "Error") 
                }
        }
    }

    fun deleteParte(servicioId: String, parteId: String) {
        viewModelScope.launch {
            servicioRepository.deleteParte(servicioId, parteId)
                .onSuccess { getPartes(servicioId) }
                .onFailure { _uiState.value = PartesTrabajoUiState.Error(it.message ?: "Error") }
        }
    }

    fun clearFieldErrors() {
        _fieldErrors.value = emptyMap()
    }

    fun canManage(): Boolean = userRole == "Administrador" || userRole == "Técnico"
}
