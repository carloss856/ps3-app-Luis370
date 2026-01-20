package com.example.inventappluis370.domain.repository

import androidx.paging.PagingData
import com.example.inventappluis370.data.model.Empresa
import com.example.inventappluis370.data.model.EmpresaRequest
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para las operaciones CRUD del módulo de Empresas.
 */
interface EmpresaRepository {

    suspend fun getEmpresas(): Result<List<Empresa>>

    suspend fun getEmpresaById(id: String): Result<Empresa>

    // Corregido: Las operaciones de escritura devuelven Unit, no el objeto.
    suspend fun createEmpresa(empresaRequest: EmpresaRequest): Result<Unit>

    suspend fun updateEmpresa(id: String, empresaRequest: EmpresaRequest): Result<Unit>

    suspend fun deleteEmpresa(id: String): Result<Unit>

    /**
     * Modo paginado (contrato dual-mode): usa query params page/per_page y recibe wrapper {data, meta}.
     */
    fun getEmpresasPaged(perPage: Int = 25): Flow<PagingData<Empresa>>
}
