package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.model.PropiedadEquipo
import com.example.inventappluis370.data.model.PropiedadEquipoRequest
import com.example.inventappluis370.data.remote.PropiedadEquipoApiService
import com.example.inventappluis370.domain.repository.PropiedadEquipoRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropiedadEquipoRepositoryImpl @Inject constructor(
    private val apiService: PropiedadEquipoApiService
) : PropiedadEquipoRepository {

    override suspend fun getPropiedades(): Result<List<PropiedadEquipo>> {
        return try {
            val response = apiService.getPropiedades()
            if (response.isSuccessful) Result.success(response.body() ?: emptyList())
            else Result.failure(IOException("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getByEquipo(equipoId: String): Result<PropiedadEquipo> {
        return try {
            val response = apiService.getByEquipo(equipoId)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(IOException("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createPropiedad(request: PropiedadEquipoRequest): Result<Unit> {
        return try {
            val response = apiService.createPropiedad(request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updatePropiedad(id: String, request: PropiedadEquipoRequest): Result<Unit> {
        return try {
            val response = apiService.updatePropiedad(id, request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deletePropiedad(id: String): Result<Unit> {
        return try {
            val response = apiService.deletePropiedad(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException("Error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // PENDIENTE: Paging Propiedad-equipos (dual-mode)
    // fun getPropiedadesPaged(perPage: Int): Flow<PagingData<PropiedadEquipo>> { ... }
}
