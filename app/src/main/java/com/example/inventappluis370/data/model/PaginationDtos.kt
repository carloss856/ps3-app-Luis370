package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Contrato de paginación (dual-mode):
 * - Sin page/per_page: el backend devuelve un array JSON.
 * - Con page/per_page: devuelve wrapper { data: [...], meta: {...} }
 *
 * Nota: En el proyecto estamos usando PaginatedResponseDto<T> como wrapper principal.
 */
@JsonClass(generateAdapter = true)
data class PagedResponseDto<T>(
    val data: List<T> = emptyList(),
    val meta: PageMetaDto? = null,
)

@JsonClass(generateAdapter = true)
data class PageMetaDto(
    val page: Int? = null,
    @Json(name = "per_page") val perPage: Int? = null,
    val total: Int? = null,
    @Json(name = "total_pages") val totalPages: Int? = null,
    @Json(name = "has_prev") val hasPrev: Boolean? = null,
    @Json(name = "has_next") val hasNext: Boolean? = null,
)
