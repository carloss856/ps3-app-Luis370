package com.example.inventappluis370.ui.permisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.PermissionsOverrideRequest
import com.example.inventappluis370.data.model.PermissionsResponse
import com.example.inventappluis370.domain.repository.PermissionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermisosViewModel @Inject constructor(
    private val repo: PermissionsRepository,
) : ViewModel() {

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data class Error(val message: String) : UiState()
        data class Ok(val data: PermissionsResponse) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    enum class Mode { GLOBAL, USER }

    private val _mode = MutableStateFlow(Mode.GLOBAL)
    val mode: StateFlow<Mode> = _mode.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    /** Mapa editable (módulo -> acciones) */
    private val _draftModules = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val draftModules: StateFlow<Map<String, Set<String>>> = _draftModules.asStateFlow()

    private fun normalizeDraftModules(input: Map<String, Set<String>>): Map<String, Set<String>> {
        return input.mapValues { (_, set) ->
            val lower = set.map { it.lowercase() }.toMutableSet()
            // Regla del proyecto: show es equivalente a index (Ver)
            if (lower.contains("show") && !lower.contains("index")) lower.add("index")
            if (lower.contains("index") && !lower.contains("show")) lower.add("show")
            lower
        }
    }

    private fun applyResponse(res: PermissionsResponse) {
        _uiState.value = UiState.Ok(res)
        val base = res.resolvedEditableModules().mapValues { it.value.toSet() }
        _draftModules.value = normalizeDraftModules(base)
    }

    fun setGlobalMode() {
        _mode.value = Mode.GLOBAL
        _userId.value = null
    }

    fun setUserMode(id: String) {
        _mode.value = Mode.USER
        _userId.value = id
    }

    fun loadGlobal() {
        setGlobalMode()
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.getGlobal()
                .onSuccess { res -> applyResponse(res) }
                .onFailure { e -> _uiState.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun loadForUser(id: String) {
        setUserMode(id)
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.getForUser(id)
                .onSuccess { res -> applyResponse(res) }
                .onFailure { e -> _uiState.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun toggleAction(moduleKey: String, action: String) {
        _draftModules.update { prev ->
            val current = prev[moduleKey].orEmpty().toMutableSet()
            val a = action.lowercase()
            if (current.contains(a)) current.remove(a) else current.add(a)

            // Mantener invariantes show<->index
            if (a == "index" || a == "show") {
                if (current.contains("index") || current.contains("show")) {
                    current.add("index")
                    current.add("show")
                } else {
                    current.remove("index")
                    current.remove("show")
                }
            }

            prev.toMutableMap().apply { put(moduleKey, current) }
        }
    }

    fun saveGlobal() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val body = PermissionsOverrideRequest(
                modules = normalizeDraftModules(_draftModules.value)
                    .mapValues { it.value.toList().sorted() }
            )
            repo.putGlobal(body)
                .onSuccess { res -> applyResponse(res) }
                .onFailure { e -> _uiState.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun saveForUser(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val body = PermissionsOverrideRequest(
                modules = normalizeDraftModules(_draftModules.value)
                    .mapValues { it.value.toList().sorted() }
            )
            repo.putForUser(id, body)
                .onSuccess { res -> applyResponse(res) }
                .onFailure { e -> _uiState.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun resetGlobal() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.resetGlobal()
                .onSuccess { res -> applyResponse(res) }
                .onFailure { e -> _uiState.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun resetForUser(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.resetForUser(id)
                .onSuccess { res -> applyResponse(res) }
                .onFailure { e -> _uiState.value = UiState.Error(e.message ?: "Error") }
        }
    }
}
