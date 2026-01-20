package com.example.inventappluis370.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.inventappluis370.data.model.Empresa
import com.example.inventappluis370.data.model.EmpresaRequest
import com.example.inventappluis370.data.paging.GenericPagingSource
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.EmpresaApiService
import com.example.inventappluis370.domain.repository.EmpresaRepository
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmpresaRepositoryImpl @Inject constructor(
    private val apiService: EmpresaApiService
) : EmpresaRepository {

    override suspend fun getEmpresas(): Result<List<Empresa>> {
        return try {
            val response = apiService.getEmpresas()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEmpresaById(id: String): Result<Empresa> {
        return try {
            val response = apiService.getEmpresaById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createEmpresa(empresaRequest: EmpresaRequest): Result<Unit> {
        return try {
            val response = apiService.createEmpresa(empresaRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                // Captura el mensaje de "ya está siendo usado" o similar del servidor
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmpresa(id: String, empresaRequest: EmpresaRequest): Result<Unit> {
        return try {
            val response = apiService.updateEmpresa(id, empresaRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEmpresa(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteEmpresa(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getEmpresasPaged(perPage: Int): Flow<PagingData<Empresa>> {
        return Pager(
            config = PagingConfig(
                pageSize = perPage,
                enablePlaceholders = false,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                GenericPagingSource(
                    perPage = perPage,
                    fetch = { page, pp -> apiService.getEmpresasPaged(page = page, perPage = pp) }
                )
            }
        ).flow
    }
}
