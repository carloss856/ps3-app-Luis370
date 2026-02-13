package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RbacResponse(
    @Json(name = "schemaVersion")
    val schemaVersion: Int? = null,

    @Json(name = "role")
    val role: String? = null,

    /** moduleKey -> acciones (index/show/store/update/destroy/etc.) */
    @Json(name = "modules")
    val modules: Map<String, List<String>> = emptyMap(),

    /** nombres de ruta permitidos */
    @Json(name = "routes")
    val routes: List<String> = emptyList(),
)
