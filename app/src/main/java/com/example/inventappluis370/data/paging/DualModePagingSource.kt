package com.example.inventappluis370.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.inventappluis370.data.model.PagedResponseDto
import com.example.inventappluis370.data.remote.DualModePageParser
import com.example.inventappluis370.data.remote.ApiErrorParser
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * PagingSource para endpoints index con contrato dual-mode.
 *
 * Estrategia:
 * - Siempre llama al endpoint en modo paginado (page/per_page).
 * - Tolera que el backend responda legacy array (por compatibilidad) y en ese caso:
 *   - devuelve una sola página (nextKey=null) porque no hay meta.
 */
class DualModePagingSource<T : Any>(
    private val perPage: Int,
    private val moshi: Moshi,
    private val itemClass: Class<T>,
    private val fetchRaw: suspend (page: Int, perPage: Int) -> Response<ResponseBody>,
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition)
        return anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 1
        return try {
            val response = fetchRaw(page, perPage)
            if (!response.isSuccessful) {
                val msg = ApiErrorParser.parseError(response)
                return LoadResult.Error(RuntimeException("HTTP ${response.code()}: $msg"))
            }

            val body = response.body() ?: return LoadResult.Error(RuntimeException("Respuesta vacía"))
            val parsed = DualModePageParser.parse(body = body, moshi = moshi, itemClass = itemClass)

            when (parsed) {
                is DualModePageParser.ParsedPage.LegacyList -> {
                    LoadResult.Page(
                        data = parsed.items,
                        prevKey = null,
                        nextKey = null
                    )
                }

                is DualModePageParser.ParsedPage.Paged -> {
                    val pageDto: PagedResponseDto<T> = parsed.page
                    val meta = pageDto.meta

                    val prevKey = if (meta?.hasPrev == true && page > 1) page - 1 else null
                    val nextKey = if (meta?.hasNext == true) page + 1 else null

                    LoadResult.Page(
                        data = pageDto.data,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }
            }
        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }
}

