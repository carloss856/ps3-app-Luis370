package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.model.RMA
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.RmaApiService
import com.example.inventappluis370.domain.repository.RmaRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RmaRepositoryImpl @Inject constructor(
    private val apiService: RmaApiService
) : RmaRepository {

    override suspend fun getRmas(): Result<List<RMA>> {
        return try {
            val response = apiService.getRmas()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRma(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteRma(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // PENDIENTE: Paging RMA (dual-mode)
    // fun getRmasPaged(perPage: Int): Flow<PagingData<RMA>> { ... }
}
