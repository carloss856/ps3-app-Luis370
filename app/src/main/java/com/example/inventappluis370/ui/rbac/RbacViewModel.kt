package com.example.inventappluis370.ui.rbac

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventappluis370.data.model.RbacResponse
import com.example.inventappluis370.domain.repository.RbacRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RbacViewModel @Inject constructor(
    private val rbacRepository: RbacRepository,
) : ViewModel() {

    val rbacFlow: StateFlow<RbacResponse?> = rbacRepository.rbacFlow

    fun refresh() {
        viewModelScope.launch { rbacRepository.refreshRbac() }
    }
}
