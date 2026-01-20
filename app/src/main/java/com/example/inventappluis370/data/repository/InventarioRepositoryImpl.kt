package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.model.Inventario
import com.example.inventappluis370.data.model.InventarioRequest
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.InventarioApiService
import com.example.inventappluis370.domain.repository.InventarioRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventarioRepositoryImpl @Inject constructor(
    private val apiService: InventarioApiService
) : InventarioRepository {

    override suspend fun getInventario(): Result<List<Inventario>> {
        return try {
            val response = apiService.getInventario()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getInventarioById(id: String): Result<Inventario> {
        return try {
            val response = apiService.getInventarioById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createInventarioEntrada(request: InventarioRequest): Result<Unit> {
        return try {
            val response = apiService.createInventarioEntrada(request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateInventarioEntrada(id: String, request: InventarioRequest): Result<Unit> {
        return try {
            val response = apiService.updateInventarioEntrada(id, request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteInventarioEntrada(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteInventarioEntrada(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) { Result.failure(e) }
    }

    // PENDIENTE: Paging Inventario (dual-mode)
    // fun getInventarioPaged(perPage: Int): Flow<PagingData<Inventario>> { ... }
}
