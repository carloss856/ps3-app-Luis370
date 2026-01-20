package com.example.inventappluis370.ui.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.Reporte
import com.example.inventappluis370.data.model.ReporteRequest
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.ReporteRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ReportesUiState {
    object Loading : ReportesUiState()
    data class Success(val reportes: List<Reporte>) : ReportesUiState()
    data class Error(val message: String) : ReportesUiState()
    object OperationSuccess : ReportesUiState()
}

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val reporteRepository: ReporteRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportesUiState>(ReportesUiState.Loading)
    val uiState: StateFlow<ReportesUiState> = _uiState.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    init {
        getReportes()
    }

    fun getReportes() {
        viewModelScope.launch {
            _uiState.value = ReportesUiState.Loading
            reporteRepository.getReportes()
                .onSuccess { reportes -> _uiState.value = ReportesUiState.Success(reportes) }
                .onFailure { _uiState.value = ReportesUiState.Error(it.message ?: "Error") }
        }
    }

    fun createReporte(tipoReporte: String, parametros: String) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: ""
            _uiState.value = ReportesUiState.Loading
            val request = ReporteRequest(tipoReporte, parametros, userId)
            reporteRepository.createReporte(request)
                .onSuccess { _uiState.value = ReportesUiState.OperationSuccess }
                .onFailure { _uiState.value = ReportesUiState.Error(it.message ?: "Error") }
        }
    }

    fun deleteReporte(id: String) {
        viewModelScope.launch {
            reporteRepository.deleteReporte(id)
                .onSuccess { getReportes() }
                .onFailure { _uiState.value = ReportesUiState.Error(it.message ?: "Error") }
        }
    }

    fun canCreate(): Boolean = PermissionManager.canCreate(userRole, "Reportes")
    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "Reportes")
}
