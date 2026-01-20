package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.LoginRequest
import com.example.inventappluis370.data.model.LoginResponse

interface AuthRepository {
    suspend fun login(loginRequest: LoginRequest): Result<LoginResponse>
    suspend fun logout()

    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun verifyToken(email: String, token: String): Result<Unit>
    suspend fun resetPassword(email: String, token: String, contrasena: String): Result<Unit>
}
