package com.example.inventappluis370.ui.equipos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.PropiedadEquipo
import com.example.inventappluis370.data.model.PropiedadEquipoRequest
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.PropiedadEquipoRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PropiedadEquipoUiState {
    object Loading : PropiedadEquipoUiState()
    data class Success(val propiedades: List<PropiedadEquipo>) : PropiedadEquipoUiState()
    data class Error(val message: String) : PropiedadEquipoUiState()
    object OperationSuccess : PropiedadEquipoUiState()
}

@HiltViewModel
class PropiedadEquipoViewModel @Inject constructor(
    private val repository: PropiedadEquipoRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PropiedadEquipoUiState>(PropiedadEquipoUiState.Loading)
    val uiState: StateFlow<PropiedadEquipoUiState> = _uiState.asStateFlow()

    private val _selectedPropiedad = MutableStateFlow<PropiedadEquipo?>(null)
    val selectedPropiedad: StateFlow<PropiedadEquipo?> = _selectedPropiedad.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    init {
        getPropiedades()
    }

    fun getPropiedades() {
        viewModelScope.launch {
            _uiState.value = PropiedadEquipoUiState.Loading
            repository.getPropiedades()
                .onSuccess { _uiState.value = PropiedadEquipoUiState.Success(it) }
                .onFailure { _uiState.value = PropiedadEquipoUiState.Error(it.message ?: "Error") }
        }
    }

    fun getPropiedadById(id: String) {
        val current = (uiState.value as? PropiedadEquipoUiState.Success)?.propiedades
        // Corregido: Usar idPropiedad para la búsqueda
        _selectedPropiedad.value = current?.find { it.idPropiedad == id }
    }

    fun createPropiedad(request: PropiedadEquipoRequest) {
        viewModelScope.launch {
            _uiState.value = PropiedadEquipoUiState.Loading
            repository.createPropiedad(request)
                .onSuccess { _uiState.value = PropiedadEquipoUiState.OperationSuccess }
                .onFailure { _uiState.value = PropiedadEquipoUiState.Error(it.message ?: "Error") }
        }
    }

    fun updatePropiedad(id: String, request: PropiedadEquipoRequest) {
        viewModelScope.launch {
            _uiState.value = PropiedadEquipoUiState.Loading
            repository.updatePropiedad(id, request)
                .onSuccess { _uiState.value = PropiedadEquipoUiState.OperationSuccess }
                .onFailure { _uiState.value = PropiedadEquipoUiState.Error(it.message ?: "Error") }
        }
    }

    fun deletePropiedad(id: String) {
        viewModelScope.launch {
            repository.deletePropiedad(id)
                .onSuccess { getPropiedades() }
                .onFailure { _uiState.value = PropiedadEquipoUiState.Error(it.message ?: "Error") }
        }
    }

    fun canCreate(): Boolean = PermissionManager.canCreate(userRole, "PropiedadEquipo")
    fun canUpdate(): Boolean = PermissionManager.canUpdate(userRole, "PropiedadEquipo")
    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "PropiedadEquipo")
}
