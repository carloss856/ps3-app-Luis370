package com.example.inventappluis370.ui.garantias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.Garantia
import com.example.inventappluis370.data.model.GarantiaRequest
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.GarantiaRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GarantiasUiState {
    object Loading : GarantiasUiState()
    data class Success(val garantias: List<Garantia>) : GarantiasUiState()
    data class Error(val message: String) : GarantiasUiState()
    object OperationSuccess : GarantiasUiState()
}

@HiltViewModel
class GarantiasViewModel @Inject constructor(
    private val garantiaRepository: GarantiaRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GarantiasUiState>(GarantiasUiState.Loading)
    val uiState: StateFlow<GarantiasUiState> = _uiState.asStateFlow()

    private val _selectedGarantia = MutableStateFlow<Garantia?>(null)
    val selectedGarantia: StateFlow<Garantia?> = _selectedGarantia.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    init {
        getGarantias()
    }

    fun getGarantias() {
        viewModelScope.launch {
            _uiState.value = GarantiasUiState.Loading
            garantiaRepository.getGarantias()
                .onSuccess { _uiState.value = GarantiasUiState.Success(it) }
                .onFailure { _uiState.value = GarantiasUiState.Error(it.message ?: "Error") }
        }
    }

    fun getGarantiaById(id: String) {
        val current = (uiState.value as? GarantiasUiState.Success)?.garantias
        // Corregido: Buscar por idGarantia
        _selectedGarantia.value = current?.find { it.idGarantia == id }
    }

    fun createGarantia(request: GarantiaRequest) {
        viewModelScope.launch {
            _uiState.value = GarantiasUiState.Loading
            garantiaRepository.createGarantia(request)
                .onSuccess { _uiState.value = GarantiasUiState.OperationSuccess }
                .onFailure { _uiState.value = GarantiasUiState.Error(it.message ?: "Error") }
        }
    }

    fun updateGarantia(id: String, request: GarantiaRequest) {
        viewModelScope.launch {
            _uiState.value = GarantiasUiState.Loading
            garantiaRepository.updateGarantia(id, request)
                .onSuccess { _uiState.value = GarantiasUiState.OperationSuccess }
                .onFailure { _uiState.value = GarantiasUiState.Error(it.message ?: "Error") }
        }
    }

    fun deleteGarantia(id: String) {
        viewModelScope.launch {
            garantiaRepository.deleteGarantia(id)
                .onSuccess { getGarantias() }
                .onFailure { _uiState.value = GarantiasUiState.Error(it.message ?: "Error") }
        }
    }

    fun canCreate(): Boolean = PermissionManager.canCreate(userRole, "Garantías")
    fun canUpdate(): Boolean = PermissionManager.canUpdate(userRole, "Garantías")
    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "Garantías")
}
