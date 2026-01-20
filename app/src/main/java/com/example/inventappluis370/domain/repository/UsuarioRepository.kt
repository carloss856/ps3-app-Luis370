package com.example.inventappluis370.domain.repository

import androidx.paging.PagingData
import com.example.inventappluis370.data.common.ApiResult
import com.example.inventappluis370.data.model.NotificacionSettings
import com.example.inventappluis370.data.model.UserRequest
import com.example.inventappluis370.data.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para las operaciones CRUD del módulo de Usuarios.
 */
interface UsuarioRepository {

    suspend fun getUsers(): Result<List<Usuario>>

    // Corregido: Las operaciones de escritura devuelven Unit.
    suspend fun createUser(userRequest: UserRequest): Result<Unit>

    // Corregido: Las operaciones de escritura devuelven Unit.
    suspend fun updateUser(id: String, userRequest: UserRequest): Result<Unit>

    /**
     * Versiones ricas (ApiResult) para poder manejar 422 con errores por campo.
     * Las usamos en formularios.
     */
    suspend fun createUserResult(userRequest: UserRequest): ApiResult<Unit>
    suspend fun updateUserResult(id: String, userRequest: UserRequest): ApiResult<Unit>

    suspend fun deleteUser(id: String): Result<Unit>

    suspend fun getNotificationSettings(userId: String): Result<NotificacionSettings>

    suspend fun saveNotificationSettings(userId: String, settings: NotificacionSettings): Result<NotificacionSettings>

    /**
     * Modo paginado (contrato dual-mode): usa query params page/per_page y recibe wrapper {data, meta}.
     */
    fun getUsersPaged(perPage: Int = 25): Flow<PagingData<Usuario>>
}
