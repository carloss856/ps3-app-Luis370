package com.example.inventappluis370.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.Inventario
import com.example.inventappluis370.data.model.InventarioRequest
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.InventarioRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class InventarioUiState {
    object Loading : InventarioUiState()
    data class Success(val inventario: List<Inventario>) : InventarioUiState()
    data class Error(val message: String) : InventarioUiState()
    object OperationSuccess : InventarioUiState()
}

@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InventarioUiState>(InventarioUiState.Loading)
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    init {
        getInventario()
    }

    fun getInventario() {
        viewModelScope.launch {
            _uiState.value = InventarioUiState.Loading
            inventarioRepository.getInventario()
                .onSuccess { _uiState.value = InventarioUiState.Success(it) }
                .onFailure { _uiState.value = InventarioUiState.Error(it.message ?: "Error") }
        }
    }

    fun createInventarioEntrada(repuestoId: String, cantidad: Int) {
        viewModelScope.launch {
            _uiState.value = InventarioUiState.Loading
            val request = InventarioRequest(repuestoId, cantidad)
            inventarioRepository.createInventarioEntrada(request)
                .onSuccess { _uiState.value = InventarioUiState.OperationSuccess }
                .onFailure { _uiState.value = InventarioUiState.Error(it.message ?: "Error") }
        }
    }

    fun deleteInventario(id: String) {
        viewModelScope.launch {
            inventarioRepository.deleteInventarioEntrada(id)
                .onSuccess { getInventario() }
                .onFailure { _uiState.value = InventarioUiState.Error(it.message ?: "Error") }
        }
    }

    fun canCreate(): Boolean = PermissionManager.canCreate(userRole, "Inventario")
    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "Inventario")
}
