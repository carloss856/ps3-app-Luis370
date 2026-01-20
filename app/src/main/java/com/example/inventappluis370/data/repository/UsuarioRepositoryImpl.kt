package com.example.inventappluis370.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.inventappluis370.data.common.ApiResult
import com.example.inventappluis370.data.common.safeApiCall
import com.example.inventappluis370.data.model.NotificacionSettings
import com.example.inventappluis370.data.model.UserRequest
import com.example.inventappluis370.data.model.Usuario
import com.example.inventappluis370.data.paging.GenericPagingSource
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.UsuarioApiService
import com.example.inventappluis370.domain.repository.UsuarioRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class UsuarioRepositoryImpl @Inject constructor(
    private val apiService: UsuarioApiService,
    private val moshi: com.squareup.moshi.Moshi
) : UsuarioRepository {

    override suspend fun getUsers(): Result<List<Usuario>> {
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createUser(userRequest: UserRequest): Result<Unit> {
        return try {
            val response = apiService.createUser(userRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                // Captura errores como "email ya registrado"
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateUser(id: String, userRequest: UserRequest): Result<Unit> {
        return try {
            val response = apiService.updateUser(id, userRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteUser(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getNotificationSettings(userId: String): Result<NotificacionSettings> {
        return try {
            val response = apiService.getNotificationSettings(userId)
            if (response.isSuccessful) {
                val body = response.body() ?: NotificacionSettings(
                    recibirNotificaciones = true,
                    tiposNotificacion = emptyList()
                )
                Result.success(body)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun saveNotificationSettings(userId: String, settings: NotificacionSettings): Result<NotificacionSettings> {
        return try {
            val response = apiService.saveNotificationSettings(userId, settings)
            if (response.isSuccessful) {
                // contrato: {message}. El estado actual queda igual a lo enviado.
                Result.success(settings)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createUserResult(userRequest: UserRequest): ApiResult<Unit> {
        // El endpoint devuelve 200/201 sin cuerpo parseable; por eso la interfaz Retrofit retorna Response<Unit>
        return safeApiCall { apiService.createUser(userRequest) }
    }

    override suspend fun updateUserResult(id: String, userRequest: UserRequest): ApiResult<Unit> {
        return safeApiCall { apiService.updateUser(id, userRequest) }
    }

    override fun getUsersPaged(perPage: Int): Flow<PagingData<Usuario>> {
        return Pager(
            config = PagingConfig(
                pageSize = perPage,
                enablePlaceholders = false,
                prefetchDistance = 2
            ),
            pagingSourceFactory = {
                com.example.inventappluis370.data.paging.DualModePagingSource(
                    perPage = perPage,
                    moshi = moshi,
                    itemClass = Usuario::class.java,
                    fetchRaw = { page, pp ->
                        apiService.getUsersPagedRaw(page = page, perPage = pp)
                    }
                )
            }
        ).flow
    }
}
