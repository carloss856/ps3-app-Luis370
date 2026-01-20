package com.example.inventappluis370.domain.repository

import com.example.inventappluis370.data.model.AutenticacionUsuario

interface AutenticacionUsuarioRepository {
    suspend fun getAutenticaciones(): Result<List<AutenticacionUsuario>>

    /**
     * (PENDIENTE) Modo paginado (contrato dual-mode).
     */
    // fun getAutenticacionesPaged(perPage: Int = 25): Flow<PagingData<AutenticacionUsuario>>
}
