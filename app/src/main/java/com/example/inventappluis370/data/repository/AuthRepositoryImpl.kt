package com.example.inventappluis370.data.repository

import android.util.Log
import com.example.inventappluis370.data.model.*
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.AuthApiService
import com.example.inventappluis370.domain.repository.AuthRepository
import com.example.inventappluis370.domain.repository.TokenRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val tokenRepository: TokenRepository
) : AuthRepository {

    override suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> {
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful) {
                val loginResponse = response.body() ?: return Result.failure(IOException("Respuesta vacía"))

                val token = loginResponse.token
                    ?: return Result.failure(IOException("Respuesta inválida: falta token"))

                val usuario = loginResponse.usuario

                // Guardar nombre visible para UI (menú principal). Si el backend no trae apellido,
                // mostramos al menos `nombre`.
                tokenRepository.saveUserDisplayName(usuario?.nombre)

                // Contrato: id principal de negocio = id_persona. El _id de Mongo se guarda aparte solo
                // para endpoints explícitos (p.ej. /usuarios/{id}/notificaciones).
                val businessId = usuario?.idPersona
                val mongoId = usuario?.mongoIdResolved()

                if (!mongoId.isNullOrBlank()) {
                    tokenRepository.saveMongoUserId(mongoId)
                }

                tokenRepository.saveSession(
                    token = token,
                    role = loginResponse.tipo ?: (usuario?.tipo ?: ""),
                    userId = businessId ?: "",
                    expiresAt = loginResponse.expiresAt ?: ""
                )
                Result.success(loginResponse)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            apiService.logout()
        } catch (e: Exception) {
            Log.w("AuthRepository", "Fallo al cerrar sesión", e)
        }
        tokenRepository.clearSession()
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyToken(email: String, token: String): Result<Unit> {
        return try {
            val response = apiService.verifyToken(VerifyTokenRequest(email, token))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String, token: String, contrasena: String): Result<Unit> {
        return try {
            val response = apiService.resetPassword(ResetPasswordRequest(email, token, contrasena))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
