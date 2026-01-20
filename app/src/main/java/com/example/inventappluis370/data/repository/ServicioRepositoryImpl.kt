package com.example.inventappluis370.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.inventappluis370.data.common.ApiResult
import com.example.inventappluis370.data.common.safeApiCall
import com.example.inventappluis370.data.model.*
import com.example.inventappluis370.data.paging.DualModePagingSource
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.PartesResponse
import com.example.inventappluis370.data.remote.ServicioApiService
import com.example.inventappluis370.domain.repository.ServicioRepository
import com.squareup.moshi.Moshi
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ServicioRepositoryImpl @Inject constructor(
    private val apiService: ServicioApiService,
    private val moshi: Moshi
) : ServicioRepository {

    override suspend fun getServicios(): Result<List<Servicio>> {
        return try {
            val response = apiService.getServicios()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getServicioById(id: String): Result<Servicio> {
        return try {
            val response = apiService.getServicioById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createServicio(request: CreateServicioRequest): Result<Unit> {
        return try {
            val response = apiService.createServicio(request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateServicio(id: String, request: CreateServicioRequest): Result<Unit> {
        return try {
            val response = apiService.updateServicio(id, request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteServicio(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteServicio(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- Partes de Trabajo ---

    override suspend fun getPartes(servicioId: String): Result<PartesResponse> {
        return try {
            val response = apiService.getPartes(servicioId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createParte(servicioId: String, request: CreateParteTrabajoRequest): Result<Unit> {
        return try {
            val response = apiService.createParte(servicioId, request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateParte(servicioId: String, parteId: String, request: UpdateParteTrabajoRequest): Result<Unit> {
        return try {
            val response = apiService.updateParte(servicioId, parteId, request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteParte(servicioId: String, parteId: String): Result<Unit> {
        return try {
            val response = apiService.deleteParte(servicioId, parteId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IOException(ApiErrorParser.parseError(response)))
        } catch (e: Exception) { Result.failure(e) }
    }

    // PENDIENTE: Paging Servicios (dual-mode)
    override fun getServiciosPaged(perPage: Int): Flow<PagingData<Servicio>> {
        return Pager(
            config = PagingConfig(
                pageSize = perPage,
                enablePlaceholders = false,
                prefetchDistance = 2
            ),
            pagingSourceFactory = {
                DualModePagingSource(
                    perPage = perPage,
                    moshi = moshi,
                    itemClass = Servicio::class.java,
                    fetchRaw = { page, pp -> apiService.getServiciosRaw(page = page, perPage = pp) }
                )
            }
        ).flow
    }

    // --- ApiResult Methods ---

    override suspend fun createServicioResult(request: CreateServicioRequest): ApiResult<Unit> {
        return safeApiCall { apiService.createServicio(request) }
    }

    override suspend fun updateServicioResult(id: String, request: CreateServicioRequest): ApiResult<Unit> {
        return safeApiCall { apiService.updateServicio(id, request) }
    }

    override suspend fun createParteResult(servicioId: String, request: CreateParteTrabajoRequest): ApiResult<Unit> {
        return safeApiCall { apiService.createParte(servicioId, request) }
    }

    override suspend fun updateParteResult(servicioId: String, parteId: String, request: UpdateParteTrabajoRequest): ApiResult<Unit> {
        return safeApiCall { apiService.updateParte(servicioId, parteId, request) }
    }
}
