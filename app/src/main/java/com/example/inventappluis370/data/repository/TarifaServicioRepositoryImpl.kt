package com.example.inventappluis370.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.inventappluis370.data.common.ApiResult
import com.example.inventappluis370.data.common.safeApiCall
import com.example.inventappluis370.data.model.CreateTarifaRequest
import com.example.inventappluis370.data.model.TarifaServicio
import com.example.inventappluis370.data.model.UpdateTarifaRequest
import com.example.inventappluis370.data.paging.GenericPagingSource
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.TarifaServicioApiService
import com.example.inventappluis370.domain.repository.TarifaServicioRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class TarifaServicioRepositoryImpl @Inject constructor(
    private val apiService: TarifaServicioApiService
) : TarifaServicioRepository {

    override suspend fun getTarifas(): Result<List<TarifaServicio>> {
        return try {
            val response = apiService.getTarifas()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTarifa(request: CreateTarifaRequest): Result<Unit> {
        return try {
            val response = apiService.createTarifa(request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException("Error: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTarifa(id: String, request: UpdateTarifaRequest): Result<Unit> {
        return try {
            val response = apiService.updateTarifa(id, request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException("Error: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTarifa(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteTarifa(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException("Error: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHistorialTarifa(id: String): Result<List<TarifaServicio>> {
        return try {
            val response = apiService.getHistorialTarifa(id)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTarifaResult(request: CreateTarifaRequest): ApiResult<Unit> {
        return safeApiCall { apiService.createTarifa(request) }
    }

    override suspend fun updateTarifaResult(id: String, request: UpdateTarifaRequest): ApiResult<Unit> {
        return safeApiCall { apiService.updateTarifa(id, request) }
    }

    override fun getTarifasPaged(perPage: Int): Flow<PagingData<TarifaServicio>> {
        return Pager(
            config = PagingConfig(
                pageSize = perPage,
                enablePlaceholders = false,
                prefetchDistance = 2
            ),
            pagingSourceFactory = {
                GenericPagingSource(perPage = perPage) { page, pp ->
                    apiService.getTarifasPaged(page = page, perPage = pp)
                }
            }
        ).flow
    }
}
