package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.PermissionsOverrideRequest
import com.example.inventappluis370.data.model.PermissionsResponse

interface PermissionsRepository {
    suspend fun getGlobal(): Result<PermissionsResponse>
    suspend fun putGlobal(body: PermissionsOverrideRequest): Result<PermissionsResponse>
    suspend fun resetGlobal(): Result<PermissionsResponse>

    suspend fun getForUser(id: String): Result<PermissionsResponse>
    suspend fun putForUser(id: String, body: PermissionsOverrideRequest): Result<PermissionsResponse>
    suspend fun resetForUser(id: String): Result<PermissionsResponse>
}
