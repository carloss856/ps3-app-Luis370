package com.example.inventappluis370.data.model

import com.squareup.moshi.JsonClass

/**
 * Wrapper para paginación: { data: [...], meta: {...} }
 */
@JsonClass(generateAdapter = true)
data class PaginatedResponseDto<T>(
    val data: List<T> = emptyList(),
    val meta: PageMetaDto? = null
)
