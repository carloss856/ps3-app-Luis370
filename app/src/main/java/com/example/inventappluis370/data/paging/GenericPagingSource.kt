package com.example.inventappluis370.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.inventappluis370.data.model.PaginatedResponseDto
import retrofit2.Response

/**
 * PagingSource genérico para endpoints dual-mode.
 *
 * Contrato:
 * - Con page/per_page => wrapper {data, meta}
 *
 * Este PagingSource siempre usa el modo paginado (con params).
 */
class GenericPagingSource<T : Any>(
    private val perPage: Int,
    private val fetch: suspend (page: Int, perPage: Int) -> Response<PaginatedResponseDto<T>>
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition)
        return anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 1
        return try {
            val response = fetch(page, perPage)
            if (!response.isSuccessful) {
                return LoadResult.Error(RuntimeException("HTTP ${response.code()}"))
            }

            val body = response.body() ?: return LoadResult.Error(RuntimeException("Respuesta vacía"))
            val data = body.data
            val meta = body.meta

            val hasPrev = meta?.hasPrev ?: (page > 1)
            val hasNext = meta?.hasNext ?: (data.size >= perPage)

            val prevKey = if (hasPrev && page > 1) page - 1 else null
            val nextKey = if (hasNext) page + 1 else null

            LoadResult.Page(
                data = data,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }
}
