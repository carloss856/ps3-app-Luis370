package com.example.inventappluis370.data.mapper

import com.example.inventappluis370.data.model.LoginResponse
import com.example.inventappluis370.domain.model.AuthSession

fun LoginResponse.toDomainSession(): AuthSession {
    val user = usuario
    val mongoId = user?.mongoIdResolved()
    val resolvedUserId = user?.idPersona ?: mongoId

    return AuthSession(
        token = token.orEmpty(),
        expiresAt = expiresAt,
        role = tipo ?: user?.tipo,
        userId = resolvedUserId,
        mongoUserId = mongoId,
    )
}
