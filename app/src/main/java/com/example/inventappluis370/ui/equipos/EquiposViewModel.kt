package com.example.inventappluis370.ui.equipos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.Equipo
import com.example.inventappluis370.data.model.EquipoRequest
import com.example.inventappluis370.domain.repository.EquipoRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import com.example.inventappluis370.domain.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EquiposUiState {
    object Loading : EquiposUiState()
    data class Success(val equipos: List<Equipo>) : EquiposUiState()
    data class Error(val message: String) : EquiposUiState()
    object OperationSuccess : EquiposUiState()
}

@HiltViewModel
class EquiposViewModel @Inject constructor(
    private val equipoRepository: EquipoRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EquiposUiState>(EquiposUiState.Loading)
    val uiState: StateFlow<EquiposUiState> = _uiState.asStateFlow()

    private val _selectedEquipo = MutableStateFlow<Equipo?>(null)
    val selectedEquipo: StateFlow<Equipo?> = _selectedEquipo.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    init {
        getEquipos()
    }

    fun getEquipos() {
        viewModelScope.launch {
            _uiState.value = EquiposUiState.Loading
            equipoRepository.getEquipos()
                .onSuccess { equipos -> _uiState.value = EquiposUiState.Success(equipos) }
                .onFailure { _uiState.value = EquiposUiState.Error(it.message ?: "Error") }
        }
    }

    fun getEquipoById(id: String) {
        val equipos = (uiState.value as? EquiposUiState.Success)?.equipos
        // Corregido: Usar idEquipo
        _selectedEquipo.value = equipos?.find { it.idEquipo == id }
    }

    fun createEquipo(equipoRequest: EquipoRequest) {
        viewModelScope.launch {
            _uiState.value = EquiposUiState.Loading
            equipoRepository.createEquipo(equipoRequest)
                .onSuccess { _uiState.value = EquiposUiState.OperationSuccess }
                .onFailure { _uiState.value = EquiposUiState.Error(it.message ?: "Error") }
        }
    }

    fun updateEquipo(id: String, equipoRequest: EquipoRequest) {
        viewModelScope.launch {
            _uiState.value = EquiposUiState.Loading
            equipoRepository.updateEquipo(id, equipoRequest)
                .onSuccess { _uiState.value = EquiposUiState.OperationSuccess }
                .onFailure { _uiState.value = EquiposUiState.Error(it.message ?: "Error") }
        }
    }

    fun deleteEquipo(id: String) {
        viewModelScope.launch {
            equipoRepository.deleteEquipo(id)
                .onSuccess { getEquipos() }
                .onFailure { _uiState.value = EquiposUiState.Error(it.message ?: "Error") }
        }
    }

    fun canCreate(): Boolean = PermissionManager.canCreate(userRole, "Equipos")
    fun canUpdate(): Boolean = PermissionManager.canUpdate(userRole, "Equipos")
    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "Equipos")
}
