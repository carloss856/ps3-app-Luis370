package com.example.inventappluis370.ui.tarifas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.TarifaServicioHistorial
import com.example.inventappluis370.domain.repository.TarifaServicioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TarifaHistorialUiState {
    object Loading : TarifaHistorialUiState()
    data class Success(val items: List<TarifaServicioHistorial>) : TarifaHistorialUiState()
    data class Error(val message: String) : TarifaHistorialUiState()
}

@HiltViewModel
class TarifaHistorialViewModel @Inject constructor(
    private val repository: TarifaServicioRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TarifaHistorialUiState>(TarifaHistorialUiState.Loading)
    val uiState: StateFlow<TarifaHistorialUiState> = _uiState.asStateFlow()

    fun loadHistorial(tarifaId: String) {
        viewModelScope.launch {
            _uiState.value = TarifaHistorialUiState.Loading
            repository.getHistorialTarifa(tarifaId)
                .onSuccess { _uiState.value = TarifaHistorialUiState.Success(it) }
                .onFailure { _uiState.value = TarifaHistorialUiState.Error(it.message ?: "Error") }
        }
    }
}
