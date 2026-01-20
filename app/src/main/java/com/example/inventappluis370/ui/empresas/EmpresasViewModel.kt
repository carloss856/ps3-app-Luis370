package com.example.inventappluis370.ui.empresas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.inventappluis370.data.model.Empresa
import com.example.inventappluis370.data.model.EmpresaRequest
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.EmpresaRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EmpresasUiState {
    object Loading : EmpresasUiState()
    data class Success(val empresas: List<Empresa>) : EmpresasUiState()
    data class Error(val message: String) : EmpresasUiState()
    object OperationSuccess : EmpresasUiState()
}

@HiltViewModel
class EmpresasViewModel @Inject constructor(
    private val empresaRepository: EmpresaRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmpresasUiState>(EmpresasUiState.Loading)
    val uiState: StateFlow<EmpresasUiState> = _uiState.asStateFlow()

    private val _selectedEmpresa = MutableStateFlow<Empresa?>(null)
    val selectedEmpresa: StateFlow<Empresa?> = _selectedEmpresa.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    // Nuevo (Paging): para pantallas que ya migren a lazy paging.
    val empresasPaging: Flow<PagingData<Empresa>> =
        empresaRepository.getEmpresasPaged(perPage = 25).cachedIn(viewModelScope)

    init {
        // Mantengo carga legacy para la UI actual.
        getEmpresas()
    }

    fun getEmpresas() {
        viewModelScope.launch {
            _uiState.value = EmpresasUiState.Loading
            empresaRepository.getEmpresas()
                .onSuccess { empresas -> _uiState.value = EmpresasUiState.Success(empresas) }
                .onFailure { _uiState.value = EmpresasUiState.Error(it.message ?: "Error") }
        }
    }

    fun getEmpresaById(id: String) {
        viewModelScope.launch {
            empresaRepository.getEmpresaById(id)
                .onSuccess { empresa -> _selectedEmpresa.value = empresa }
                .onFailure {
                    val empresas = (uiState.value as? EmpresasUiState.Success)?.empresas
                    _selectedEmpresa.value = empresas?.find { it.idEmpresa == id }
                }
        }
    }

    fun createEmpresa(empresaRequest: EmpresaRequest) {
        viewModelScope.launch {
            _uiState.value = EmpresasUiState.Loading
            empresaRepository.createEmpresa(empresaRequest)
                .onSuccess { _uiState.value = EmpresasUiState.OperationSuccess }
                .onFailure { _uiState.value = EmpresasUiState.Error(it.message ?: "Error al crear la empresa") }
        }
    }

    fun updateEmpresa(id: String, empresaRequest: EmpresaRequest) {
        viewModelScope.launch {
            _uiState.value = EmpresasUiState.Loading
            empresaRepository.updateEmpresa(id, empresaRequest)
                .onSuccess { _uiState.value = EmpresasUiState.OperationSuccess }
                .onFailure { _uiState.value = EmpresasUiState.Error(it.message ?: "Error al actualizar la empresa") }
        }
    }

    fun deleteEmpresa(id: String) {
        viewModelScope.launch {
            empresaRepository.deleteEmpresa(id)
                .onSuccess { getEmpresas() }
                .onFailure { _uiState.value = EmpresasUiState.Error(it.message ?: "Error") }
        }
    }

    // Funciones de permisos
    fun canCreate(): Boolean = PermissionManager.canCreate(userRole, "Empresas")
    fun canUpdate(): Boolean = PermissionManager.canUpdate(userRole, "Empresas")
    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "Empresas")
}
