package com.example.inventappluis370.ui.servicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.inventappluis370.data.common.ApiResult
import com.example.inventappluis370.data.model.CreateServicioRequest
import com.example.inventappluis370.data.model.Servicio
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.ServicioRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ServiciosUiState {
    object Loading : ServiciosUiState()
    data class Success(val servicios: List<Servicio>) : ServiciosUiState()
    data class Error(val message: String) : ServiciosUiState()
    object OperationSuccess : ServiciosUiState()
}

@HiltViewModel
class ServiciosViewModel @Inject constructor(
    private val servicioRepository: ServicioRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiciosUiState>(ServiciosUiState.Loading)
    val uiState: StateFlow<ServiciosUiState> = _uiState.asStateFlow()

    private val _selectedServicio = MutableStateFlow<Servicio?>(null)
    val selectedServicio: StateFlow<Servicio?> = _selectedServicio.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, List<String>>> = _fieldErrors.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    /**
     * Paging (dual-mode): usa query params page/per_page y wrapper {data, meta}.
     */
    val serviciosPaged: Flow<PagingData<Servicio>> =
        servicioRepository.getServiciosPaged(perPage = 25).cachedIn(viewModelScope)

    init {
        // Para evitar doble carga, dejamos que la UI decida si usa lista o paging.
        // Si tu UI usa paging, no necesita esta llamada.
        // getServicios()
    }

    fun clearFieldErrors() {
        _fieldErrors.value = emptyMap()
    }

    fun getServicios() {
        viewModelScope.launch {
            _uiState.value = ServiciosUiState.Loading
            servicioRepository.getServicios()
                .onSuccess { servicios ->
                    _uiState.value = ServiciosUiState.Success(servicios)
                }
                .onFailure { error ->
                    _uiState.value = ServiciosUiState.Error(error.message ?: "Ocurrió un error inesperado")
                }
        }
    }

    fun getServicioById(id: String) {
        val servicios = (uiState.value as? ServiciosUiState.Success)?.servicios
        _selectedServicio.value = servicios?.find { it.idServicio == id }
    }

    fun createServicio(request: CreateServicioRequest) {
        viewModelScope.launch {
            _uiState.value = ServiciosUiState.Loading
            _fieldErrors.value = emptyMap()

            when (val result = servicioRepository.createServicioResult(request)) {
                is ApiResult.Success -> _uiState.value = ServiciosUiState.OperationSuccess
                is ApiResult.Error.Validation -> {
                    _fieldErrors.value = result.fieldErrors
                    _uiState.value = ServiciosUiState.Error(result.message)
                }
                is ApiResult.Error.Unauthorized -> _uiState.value = ServiciosUiState.Error(result.message)
                is ApiResult.Error.Forbidden -> _uiState.value = ServiciosUiState.Error(result.message)
                is ApiResult.Error.Http -> _uiState.value = ServiciosUiState.Error(result.message)
                is ApiResult.Error.Network -> _uiState.value = ServiciosUiState.Error(result.message)
                is ApiResult.Error.Unknown -> _uiState.value = ServiciosUiState.Error(result.message)
            }
        }
    }

    fun updateServicio(id: String, request: CreateServicioRequest) {
        viewModelScope.launch {
            _uiState.value = ServiciosUiState.Loading
            _fieldErrors.value = emptyMap()

            when (val result = servicioRepository.updateServicioResult(id, request)) {
                is ApiResult.Success -> _uiState.value = ServiciosUiState.OperationSuccess
                is ApiResult.Error.Validation -> {
                    _fieldErrors.value = result.fieldErrors
                    _uiState.value = ServiciosUiState.Error(result.message)
                }
                is ApiResult.Error.Unauthorized -> _uiState.value = ServiciosUiState.Error(result.message)
                is ApiResult.Error.Forbidden -> _uiState.value = ServiciosUiState.Error(result.message)
                is ApiResult.Error.Http -> _uiState.value = ServiciosUiState.Error(result.message)
                is ApiResult.Error.Network -> _uiState.value = ServiciosUiState.Error(result.message)
                is ApiResult.Error.Unknown -> _uiState.value = ServiciosUiState.Error(result.message)
            }
        }
    }

    fun deleteServicio(id: String) {
        viewModelScope.launch {
            servicioRepository.deleteServicio(id)
                .onSuccess { getServicios() }
                .onFailure { _uiState.value = ServiciosUiState.Error(it.message ?: "Error al eliminar") }
        }
    }

    fun canCreate(): Boolean = PermissionManager.canCreate(userRole, "Servicios")
    fun canUpdate(): Boolean = PermissionManager.canUpdate(userRole, "Servicios")
    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "Servicios")
}
