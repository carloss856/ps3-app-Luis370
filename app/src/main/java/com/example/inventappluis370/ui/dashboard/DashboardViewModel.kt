package com.example.inventappluis370.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.domain.model.DashboardItem
import com.example.inventappluis370.domain.repository.TokenRepository
import com.example.inventappluis370.domain.usecase.GetDashboardItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardItemsUseCase: GetDashboardItemsUseCase,
    tokenRepository: TokenRepository
) : ViewModel() {

    val userRole: StateFlow<String?> = tokenRepository.userRoleFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val userDisplayName: StateFlow<String?> = tokenRepository.userDisplayNameFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val dashboardItems: StateFlow<List<DashboardItem>> = userRole.map {
        getDashboardItemsUseCase(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
