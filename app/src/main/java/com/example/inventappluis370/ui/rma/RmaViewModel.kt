package com.example.inventappluis370.ui.rma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.RMA
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.RmaRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RmaUiState {
    object Loading : RmaUiState()
    data class Success(val rmas: List<RMA>) : RmaUiState()
    data class Error(val message: String) : RmaUiState()
}

@HiltViewModel
class RmaViewModel @Inject constructor(
    private val rmaRepository: RmaRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RmaUiState>(RmaUiState.Loading)
    val uiState: StateFlow<RmaUiState> = _uiState.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    init {
        getRmas()
    }

    fun getRmas() {
        viewModelScope.launch {
            _uiState.value = RmaUiState.Loading
            rmaRepository.getRmas()
                .onSuccess { rmas -> _uiState.value = RmaUiState.Success(rmas) }
                .onFailure { _uiState.value = RmaUiState.Error(it.message ?: "Error") }
        }
    }

    fun deleteRma(id: String) {
        viewModelScope.launch {
            rmaRepository.deleteRma(id)
                .onSuccess { getRmas() }
                .onFailure { _uiState.value = RmaUiState.Error(it.message ?: "Error al eliminar RMA") }
        }
    }

    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "RMA")
}
