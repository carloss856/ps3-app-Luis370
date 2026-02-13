package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Payload canónico alineado con el backend (ver resumenweb.txt 9.10):
 * parametros_utilizados = { modules: [...], filters: {...}, source: "android" }
 */
@JsonClass(generateAdapter = true)
data class ReporteParametros(
    @Json(name = "modules")
    val modules: List<String> = emptyList(),

    /**
     * Filtros libres. Por ahora la UI Android no expone filtros, pero enviamos un objeto
     * estable para quedar a la par del web.
     */
    @Json(name = "filters")
    val filters: Map<String, Any?> = emptyMap(),

    @Json(name = "source")
    val source: String = "android",
)
