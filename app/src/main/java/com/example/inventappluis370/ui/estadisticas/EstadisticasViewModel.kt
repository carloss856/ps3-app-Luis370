package com.example.inventappluis370.ui.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.domain.model.DashboardData
import com.example.inventappluis370.domain.model.ModuleStats
import com.example.inventappluis370.domain.model.StatsPeriod
import com.example.inventappluis370.domain.repository.DashboardRepository
import com.example.inventappluis370.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EstadisticasViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val statsRepository: StatsRepository,
) : ViewModel() {

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data class Ok(val data: DashboardData) : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class ModuleUiState {
        data object Idle : ModuleUiState()
        data object Loading : ModuleUiState()
        data class Ok(val data: ModuleStats) : ModuleUiState()
        data class Error(val message: String) : ModuleUiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _periodByModule = MutableStateFlow<Map<String, StatsPeriod>>(emptyMap())
    val periodByModule: StateFlow<Map<String, StatsPeriod>> = _periodByModule.asStateFlow()

    private val _moduleUiState = MutableStateFlow<Map<String, ModuleUiState>>(emptyMap())
    val moduleUiState: StateFlow<Map<String, ModuleUiState>> = _moduleUiState.asStateFlow()

    fun loadDashboard() {
        if (_uiState.value is UiState.Loading) return
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val data = dashboardRepository.getDashboard()
                _uiState.value = UiState.Ok(data)
            } catch (t: Throwable) {
                _uiState.value = UiState.Error(t.message ?: "No se pudo cargar el dashboard")
            }
        }
    }

    fun setPeriod(module: String, period: StatsPeriod) {
        _periodByModule.update { it + (module to period) }
        refreshModule(module)
    }

    fun refreshModule(module: String, from: String? = null, to: String? = null) {
        val period = _periodByModule.value[module] ?: StatsPeriod.MONTH
        viewModelScope.launch {
            _moduleUiState.update { it + (module to ModuleUiState.Loading) }
            try {
                val stats = statsRepository.getStatsCached(module = module, period = period, from = from, to = to)
                _moduleUiState.update { it + (module to ModuleUiState.Ok(stats)) }
            } catch (t: Throwable) {
                _moduleUiState.update { it + (module to ModuleUiState.Error(t.message ?: "Error cargando estadísticas")) }
            }
        }
    }
}
