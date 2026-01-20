package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.model.Equipo
import com.example.inventappluis370.data.model.EquipoRequest
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.EquipoApiService
import com.example.inventappluis370.domain.repository.EquipoRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipoRepositoryImpl @Inject constructor(
    private val apiService: EquipoApiService
) : EquipoRepository {

    override suspend fun getEquipos(): Result<List<Equipo>> {
        return try {
            val response = apiService.getEquipos()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createEquipo(equipoRequest: EquipoRequest): Result<Unit> {
        return try {
            val response = apiService.createEquipo(equipoRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateEquipo(id: String, equipoRequest: EquipoRequest): Result<Unit> {
        return try {
            val response = apiService.updateEquipo(id, equipoRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteEquipo(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteEquipo(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // PENDIENTE: Paging Equipos (dual-mode)
    // fun getEquiposPaged(perPage: Int): Flow<PagingData<Equipo>> { ... }
}
