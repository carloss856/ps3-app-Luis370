package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.model.Garantia
import com.example.inventappluis370.data.model.GarantiaRequest
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.GarantiaApiService
import com.example.inventappluis370.domain.repository.GarantiaRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GarantiaRepositoryImpl @Inject constructor(
    private val apiService: GarantiaApiService
) : GarantiaRepository {

    override suspend fun getGarantias(): Result<List<Garantia>> {
        return try {
            val response = apiService.getGarantias()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getGarantiaById(id: String): Result<Garantia> {
        return try {
            val response = apiService.getGarantiaById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createGarantia(request: GarantiaRequest): Result<Unit> {
        return try {
            val response = apiService.createGarantia(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateGarantia(id: String, request: GarantiaRequest): Result<Unit> {
        return try {
            val response = apiService.updateGarantia(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteGarantia(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteGarantia(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // PENDIENTE: Paging Garantías (dual-mode)
    // fun getGarantiasPaged(perPage: Int): Flow<PagingData<Garantia>> { ... }
}
