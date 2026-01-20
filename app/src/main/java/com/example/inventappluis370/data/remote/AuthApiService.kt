package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<Unit>

    @POST("password/forgot")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Unit>

    @POST("password/verify")
    suspend fun verifyToken(@Body request: VerifyTokenRequest): Response<Unit>

    @POST("password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>

    @POST("token/extend")
    suspend fun extendToken(): Response<TokenExtendResponse>
}
