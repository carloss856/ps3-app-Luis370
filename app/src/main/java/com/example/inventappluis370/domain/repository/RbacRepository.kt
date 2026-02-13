package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.RbacResponse
import kotlinx.coroutines.flow.StateFlow

interface RbacRepository {
    val rbacFlow: StateFlow<RbacResponse?>

    suspend fun refreshRbac(): Result<RbacResponse>

    fun getCached(): RbacResponse?

    fun clear()
}
