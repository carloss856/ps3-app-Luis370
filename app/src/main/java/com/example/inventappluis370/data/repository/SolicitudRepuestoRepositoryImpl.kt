package com.example.inventappluis370.data.repository

import android.util.Log
import com.example.inventappluis370.BuildConfig
import com.example.inventappluis370.data.model.CreateSolicitudRequest
import com.example.inventappluis370.data.model.SolicitudRepuesto
import com.example.inventappluis370.data.model.UpdateSolicitudRequest
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.example.inventappluis370.data.remote.SolicitudRepuestoApiService
import com.example.inventappluis370.domain.repository.SolicitudRepuestoRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SolicitudRepuestoRepositoryImpl @Inject constructor(
    private val apiService: SolicitudRepuestoApiService
) : SolicitudRepuestoRepository {

    override suspend fun getSolicitudes(): Result<List<SolicitudRepuesto>> {
        return try {
            val response = apiService.getSolicitudes()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            // Si esto es un fallo de parseo (Moshi/JSON) o de conectividad, queremos verlo claro.
            Log.e("SolicitudesRepo", "getSolicitudes() falló. baseUrl=${BuildConfig.BASE_URL}", e)

            // Intento extra: pedir RAW para tener algo tangible en el error cuando el parseo rompe.
            val rawSnippet = runCatching {
                val rawResp = apiService.getSolicitudesRaw()
                val body = rawResp.body()?.string()?.take(2000)
                "rawStatus=${rawResp.code()} body=${body ?: "<null>"}"
            }.getOrNull()

            val wrapped = if (rawSnippet != null) {
                IOException("getSolicitudes() error=${e::class.java.simpleName}: ${e.message}. $rawSnippet", e)
            } else {
                e
            }

            Result.failure(wrapped)
        }
    }

    override suspend fun getSolicitudById(id: String): Result<SolicitudRepuesto> {
        return try {
            val response = apiService.getSolicitudById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Log.e("SolicitudesRepo", "getSolicitudById($id) falló. baseUrl=${BuildConfig.BASE_URL}", e)
            Result.failure(e)
        }
    }

    override suspend fun createSolicitud(request: CreateSolicitudRequest): Result<Unit> {
        return try {
            val response = apiService.createSolicitud(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Log.e("SolicitudesRepo", "createSolicitud() falló. baseUrl=${BuildConfig.BASE_URL}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateSolicitud(id: String, request: UpdateSolicitudRequest): Result<Unit> {
        return try {
            val response = apiService.updateSolicitud(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Log.e("SolicitudesRepo", "updateSolicitud($id) falló. baseUrl=${BuildConfig.BASE_URL}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteSolicitud(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteSolicitud(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException(ApiErrorParser.parseError(response)))
            }
        } catch (e: Exception) {
            Log.e("SolicitudesRepo", "deleteSolicitud($id) falló. baseUrl=${BuildConfig.BASE_URL}", e)
            Result.failure(e)
        }
    }

    // PENDIENTE: Paging Solicitud-repuestos (dual-mode)
    // fun getSolicitudesPaged(perPage: Int): Flow<PagingData<SolicitudRepuesto>> { ... }
}
