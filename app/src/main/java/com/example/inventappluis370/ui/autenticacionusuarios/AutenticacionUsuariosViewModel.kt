package com.example.inventappluis370.ui.autenticacionusuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.AutenticacionUsuario
import com.example.inventappluis370.domain.repository.AutenticacionUsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AutenticacionUsuariosUiState {
    object Loading : AutenticacionUsuariosUiState()
    data class Success(val items: List<AutenticacionUsuario>) : AutenticacionUsuariosUiState()
    data class Error(val message: String) : AutenticacionUsuariosUiState()
}

@HiltViewModel
class AutenticacionUsuariosViewModel @Inject constructor(
    private val repository: AutenticacionUsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AutenticacionUsuariosUiState>(AutenticacionUsuariosUiState.Loading)
    val uiState: StateFlow<AutenticacionUsuariosUiState> = _uiState.asStateFlow()

    init {
        getAutenticaciones()
    }

    fun getAutenticaciones() {
        viewModelScope.launch {
            _uiState.value = AutenticacionUsuariosUiState.Loading
            repository.getAutenticaciones()
                .onSuccess { _uiState.value = AutenticacionUsuariosUiState.Success(it) }
                .onFailure { _uiState.value = AutenticacionUsuariosUiState.Error(it.message ?: "Error") }
        }
    }

    // PENDIENTE: Paging Autenticacion-usuarios
    // val autenticacionesPaged: Flow<PagingData<AutenticacionUsuario>> =
    //     repository.getAutenticacionesPaged(perPage = 25).cachedIn(viewModelScope)
}
