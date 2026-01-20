package com.example.inventappluis370.ui.tarifas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.CreateTarifaRequest
import com.example.inventappluis370.data.model.TarifaServicio
import com.example.inventappluis370.data.model.UpdateTarifaRequest
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.TarifaServicioRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TarifasUiState {
    object Loading : TarifasUiState()
    data class Success(val tarifas: List<TarifaServicio>) : TarifasUiState()
    data class Error(val message: String) : TarifasUiState()
    object OperationSuccess : TarifasUiState()
}

@HiltViewModel
class TarifasViewModel @Inject constructor(
    private val repository: TarifaServicioRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TarifasUiState>(TarifasUiState.Loading)
    val uiState: StateFlow<TarifasUiState> = _uiState.asStateFlow()

    private val _selectedTarifa = MutableStateFlow<TarifaServicio?>(null)
    val selectedTarifa: StateFlow<TarifaServicio?> = _selectedTarifa.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, List<String>>> = _fieldErrors.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    init {
        getTarifas()
    }

    fun getTarifas() {
        viewModelScope.launch {
            _uiState.value = TarifasUiState.Loading
            repository.getTarifas()
                .onSuccess { _uiState.value = TarifasUiState.Success(it) }
                .onFailure { _uiState.value = TarifasUiState.Error(it.message ?: "Error") }
        }
    }

    fun getTarifaById(id: String) {
        val currentTarifas = (uiState.value as? TarifasUiState.Success)?.tarifas
        _selectedTarifa.value = currentTarifas?.find { it.idTarifa == id }
    }

    fun createTarifa(request: CreateTarifaRequest) {
        viewModelScope.launch {
            _uiState.value = TarifasUiState.Loading
            repository.createTarifa(request)
                .onSuccess { _uiState.value = TarifasUiState.OperationSuccess }
                .onFailure { _uiState.value = TarifasUiState.Error(it.message ?: "Error al crear tarifa") }
        }
    }

    fun updateTarifa(id: String, request: UpdateTarifaRequest) {
        viewModelScope.launch {
            _uiState.value = TarifasUiState.Loading
            repository.updateTarifa(id, request)
                .onSuccess { _uiState.value = TarifasUiState.OperationSuccess }
                .onFailure { _uiState.value = TarifasUiState.Error(it.message ?: "Error al actualizar tarifa") }
        }
    }

    fun deleteTarifa(id: String) {
        viewModelScope.launch {
            repository.deleteTarifa(id)
                .onSuccess { getTarifas() }
                .onFailure { _uiState.value = TarifasUiState.Error(it.message ?: "Error al eliminar tarifa") }
        }
    }

    fun clearFieldErrors() {
        _fieldErrors.value = emptyMap()
    }

    fun canCreate(): Boolean = PermissionManager.canCreate(userRole, "Tarifas")
    fun canUpdate(): Boolean = PermissionManager.canUpdate(userRole, "Tarifas")
    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "Tarifas")
}
