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

    companion object {
        const val DEFAULT_ROLE = "Administrador"
    }

    private val _mode = MutableStateFlow(Mode.GLOBAL)
    val mode: StateFlow<Mode> = _mode.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    /** Rol cuya matriz se esta editando en modo Global (irrelevante en modo Usuario). */
    private val _role = MutableStateFlow(DEFAULT_ROLE)
    val role: StateFlow<String> = _role.asStateFlow()

    /** Mapa editable (modulo -> acciones) de lo que se muestra/edita en pantalla. */
    private val _draftModules = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val draftModules: StateFlow<Map<String, Set<String>>> = _draftModules.asStateFlow()

    /**
     * Matriz completa modulo -> rol -> acciones, tal como vino del backend (modo Global).
     * Se mantiene aparte del draft visible porque al guardar hay que mandar TODOS los roles,
     * no solo el que se esta viendo/editando en pantalla (ver PermissionsOverrideRequest).
     */
    private var globalModulesByRole: Map<String, Map<String, Set<String>>> = emptyMap()

    private fun normalizeDraftModules(input: Map<String, Set<String>>): Map<String, Set<String>> {
        return input.mapValues { (_, set) ->
            val lower = set.map { it.lowercase() }.toMutableSet()
            // Regla del proyecto: show es equivalente a index (Ver)
            if (lower.contains("show") && !lower.contains("index")) lower.add("index")
            if (lower.contains("index") && !lower.contains("show")) lower.add("show")
            lower
        }
    }

    private fun sliceForRole(byRole: Map<String, Map<String, Set<String>>>, role: String): Map<String, Set<String>> {
        return byRole.mapValues { (_, roleMap) -> roleMap[role].orEmpty() }
    }

    private fun applyResponse(res: PermissionsResponse) {
        _uiState.value = UiState.Ok(res)
        if (_mode.value == Mode.GLOBAL) {
            val byRole = res.resolvedEditableModulesByRole().mapValues { (_, roleMap) ->
                normalizeDraftModules(roleMap.mapValues { it.value.toSet() })
            }
            globalModulesByRole = byRole
            _draftModules.value = sliceForRole(byRole, _role.value)
        } else {
            val base = res.resolvedEditableModules().mapValues { it.value.toSet() }
            _draftModules.value = normalizeDraftModules(base)
        }
    }

    fun setGlobalMode() {
        _mode.value = Mode.GLOBAL
        _userId.value = null
    }

    fun setUserMode(id: String) {
        _mode.value = Mode.USER
        _userId.value = id
    }

    /** Cambia el rol cuya matriz se muestra/edita en modo Global (no-op en modo Usuario). */
    fun setRole(newRole: String) {
        _role.value = newRole
        if (_mode.value == Mode.GLOBAL) {
            _draftModules.value = sliceForRole(globalModulesByRole, newRole)
        }
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

            val next = prev.toMutableMap().apply { put(moduleKey, current) }

            // En modo Global, reflejar el cambio tambien en la matriz completa (por rol) para
            // no perderlo si el usuario cambia de rol en pantalla antes de guardar.
            if (_mode.value == Mode.GLOBAL) {
                val roleKey = _role.value
                globalModulesByRole = globalModulesByRole.toMutableMap().apply {
                    val moduleRoles = (this[moduleKey] ?: emptyMap()).toMutableMap()
                    moduleRoles[roleKey] = current
                    this[moduleKey] = moduleRoles
                }
            }

            next
        }
    }

    fun saveGlobal() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            // IMPORTANTE: se manda la matriz COMPLETA (todos los roles), no solo el rol
            // seleccionado en pantalla, porque el backend reemplaza el override entero.
            val payloadModules: Map<String, Any> = globalModulesByRole.mapValues { (_, roleMap) ->
                roleMap.mapValues { it.value.toList().sorted() }
            }
            val body = PermissionsOverrideRequest(modules = payloadModules)
            repo.putGlobal(body)
                .onSuccess { res -> applyResponse(res) }
                .onFailure { e -> _uiState.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun saveForUser(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val payloadModules: Map<String, Any> = normalizeDraftModules(_draftModules.value)
                .mapValues { it.value.toList().sorted() }
            val body = PermissionsOverrideRequest(modules = payloadModules)
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
