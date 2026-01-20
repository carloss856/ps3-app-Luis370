package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.model.AutenticacionUsuario
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.AutenticacionUsuarioApiService
import com.example.inventappluis370.domain.repository.AutenticacionUsuarioRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutenticacionUsuarioRepositoryImpl @Inject constructor(
    private val apiService: AutenticacionUsuarioApiService
) : AutenticacionUsuarioRepository {

    override suspend fun getAutenticaciones(): Result<List<AutenticacionUsuario>> {
        return try {
            val response = apiService.getAutenticaciones()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // PENDIENTE: Paging Autenticación-usuarios (dual-mode)
    // fun getAutenticacionesPaged(perPage: Int): Flow<PagingData<AutenticacionUsuario>> { ... }
}
