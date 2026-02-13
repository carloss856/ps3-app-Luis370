package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.model.RbacResponse
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.RbacApiService
import com.example.inventappluis370.domain.repository.RbacRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RbacRepositoryImpl @Inject constructor(
    private val apiService: RbacApiService,
) : RbacRepository {

    private val _rbacFlow = MutableStateFlow<RbacResponse?>(null)
    override val rbacFlow: StateFlow<RbacResponse?> = _rbacFlow.asStateFlow()

    override suspend fun refreshRbac(): Result<RbacResponse> {
        return try {
            val response = apiService.getRbac()
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(IOException("RBAC vacío"))
                _rbacFlow.value = body
                Result.success(body)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCached(): RbacResponse? = _rbacFlow.value

    override fun clear() {
        _rbacFlow.value = null
    }
}
