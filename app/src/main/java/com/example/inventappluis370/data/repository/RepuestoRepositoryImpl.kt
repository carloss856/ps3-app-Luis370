package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.model.Repuesto
import com.example.inventappluis370.data.model.RepuestoRequest
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.DualModePageParser
import com.example.inventappluis370.data.remote.RepuestoApiService
import com.example.inventappluis370.domain.repository.RepuestoRepository
import com.squareup.moshi.Moshi
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepuestoRepositoryImpl @Inject constructor(
    private val apiService: RepuestoApiService,
    private val moshi: Moshi
) : RepuestoRepository {

    override suspend fun getRepuestos(): Result<List<Repuesto>> {
        return try {
            val response = apiService.getRepuestosRaw()
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.success(emptyList())
                body.use {
                    when (val parsed = DualModePageParser.parse(it, moshi, Repuesto::class.java)) {
                        is DualModePageParser.ParsedPage.LegacyList -> Result.success(parsed.items)
                        is DualModePageParser.ParsedPage.Paged -> Result.success(parsed.page.data)
                    }
                }
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRepuesto(repuestoRequest: RepuestoRequest): Result<Unit> {
        return try {
            val response = apiService.createRepuesto(repuestoRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateRepuesto(id: String, repuestoRequest: RepuestoRequest): Result<Unit> {
        return try {
            val response = apiService.updateRepuesto(id, repuestoRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteRepuesto(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteRepuesto(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // PENDIENTE: Paging Repuestos (dual-mode)
    // fun getRepuestosPaged(perPage: Int): Flow<PagingData<Repuesto>> { ... }
}
