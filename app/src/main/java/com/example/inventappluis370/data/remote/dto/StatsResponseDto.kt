package com.example.inventappluis370.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatsResponseDto(
    @Json(name = "module") val module: String? = null,
    @Json(name = "period") val period: String? = null,
    @Json(name = "from") val from: String? = null,
    @Json(name = "to") val to: String? = null,
    @Json(name = "total") val total: Int? = null,
    @Json(name = "buckets") val buckets: List<StatsBucketDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class StatsBucketDto(
    @Json(name = "label") val label: String? = null,
    @Json(name = "count") val count: Int? = null,
)
