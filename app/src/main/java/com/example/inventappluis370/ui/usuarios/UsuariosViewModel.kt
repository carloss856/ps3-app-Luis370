package com.example.inventappluis370.ui.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.inventappluis370.data.common.ApiResult
import com.example.inventappluis370.data.model.UserRequest
import com.example.inventappluis370.data.model.Usuario
import com.example.inventappluis370.domain.PermissionManager
import com.example.inventappluis370.domain.repository.UsuarioRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UsuariosUiState {
    object Loading : UsuariosUiState()
    data class Success(val users: List<Usuario>) : UsuariosUiState()
    data class Error(val message: String) : UsuariosUiState()
    object OperationSuccess : UsuariosUiState()
}

@HiltViewModel
class UsuariosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UsuariosUiState>(UsuariosUiState.Loading)
    val uiState: StateFlow<UsuariosUiState> = _uiState.asStateFlow()

    private val _selectedUser = MutableStateFlow<Usuario?>(null)
    val selectedUser: StateFlow<Usuario?> = _selectedUser.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, List<String>>> = _fieldErrors.asStateFlow()

    private val userRole: String? get() = tokenRepository.getRole()

    val usuariosPaged: Flow<PagingData<Usuario>> =
        usuarioRepository.getUsersPaged(perPage = 25).cachedIn(viewModelScope)

    fun clearFieldErrors() {
        _fieldErrors.value = emptyMap()
    }

    fun getUserById(idPersona: String) {
        val users = (uiState.value as? UsuariosUiState.Success)?.users
        _selectedUser.value = users?.find { it.idPersona == idPersona }
    }

    fun createUser(userRequest: UserRequest) {
        viewModelScope.launch {
            _uiState.value = UsuariosUiState.Loading
            _fieldErrors.value = emptyMap()

            when (val result = usuarioRepository.createUserResult(userRequest)) {
                is ApiResult.Success -> _uiState.value = UsuariosUiState.OperationSuccess
                is ApiResult.Error.Validation -> {
                    _fieldErrors.value = result.fieldErrors
                    _uiState.value = UsuariosUiState.Error(result.message)
                }
                is ApiResult.Error.Unauthorized -> _uiState.value = UsuariosUiState.Error(result.message)
                is ApiResult.Error.Forbidden -> _uiState.value = UsuariosUiState.Error(result.message)
                is ApiResult.Error.Http -> _uiState.value = UsuariosUiState.Error(result.message)
                is ApiResult.Error.Network -> _uiState.value = UsuariosUiState.Error(result.message)
                is ApiResult.Error.Unknown -> _uiState.value = UsuariosUiState.Error(result.message)
            }
        }
    }

    fun updateUser(id: String, userRequest: UserRequest) {
        viewModelScope.launch {
            _uiState.value = UsuariosUiState.Loading
            _fieldErrors.value = emptyMap()

            when (val result = usuarioRepository.updateUserResult(id, userRequest)) {
                is ApiResult.Success -> _uiState.value = UsuariosUiState.OperationSuccess
                is ApiResult.Error.Validation -> {
                    _fieldErrors.value = result.fieldErrors
                    _uiState.value = UsuariosUiState.Error(result.message)
                }
                is ApiResult.Error.Unauthorized -> _uiState.value = UsuariosUiState.Error(result.message)
                is ApiResult.Error.Forbidden -> _uiState.value = UsuariosUiState.Error(result.message)
                is ApiResult.Error.Http -> _uiState.value = UsuariosUiState.Error(result.message)
                is ApiResult.Error.Network -> _uiState.value = UsuariosUiState.Error(result.message)
                is ApiResult.Error.Unknown -> _uiState.value = UsuariosUiState.Error(result.message)
            }
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            usuarioRepository.deleteUser(id)
                .onSuccess { getUsers() }
                .onFailure { _uiState.value = UsuariosUiState.Error(it.message ?: "Error") }
        }
    }

    fun getUsers() {
        viewModelScope.launch {
            _uiState.value = UsuariosUiState.Loading
            usuarioRepository.getUsers()
                .onSuccess { users -> _uiState.value = UsuariosUiState.Success(users) }
                .onFailure { _uiState.value = UsuariosUiState.Error(it.message ?: "Error") }
        }
    }

    fun canCreate(): Boolean = PermissionManager.canCreate(userRole, "Usuarios")
    fun canUpdate(): Boolean = PermissionManager.canUpdate(userRole, "Usuarios")
    fun canDelete(): Boolean = PermissionManager.canDelete(userRole, "Usuarios")
}
