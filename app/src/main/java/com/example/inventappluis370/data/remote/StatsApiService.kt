package com.example.inventappluis370.data.remote
import com.example.inventappluis370.data.remote.dto.StatsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
interface StatsApiService {
    @GET("stats/{module}")
    suspend fun getStats(
        @Path("module") module: String,
        @Query("period") period: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): StatsResponseDto
}
