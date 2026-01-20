package com.example.inventappluis370.data.remote

import com.example.inventappluis370.core.network.LenientAnyToStringAdapter
import com.example.inventappluis370.core.network.LenientDoubleAdapter
import com.example.inventappluis370.core.network.LenientIntAdapter
import com.example.inventappluis370.core.network.LenientStringListAdapter
import com.example.inventappluis370.data.model.Repuesto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class DualModePageParserTest {

    private val moshi: Moshi = Moshi.Builder()
        .add(LenientIntAdapter)
        .add(LenientDoubleAdapter)
        .add(LenientStringListAdapter)
        .add(LenientAnyToStringAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `parsea lista legacy cuando la respuesta es un array JSON`() {
        val json = """
            [
              {"id_repuesto": 1, "nombre_repuesto": "Filtro", "cantidad_disponible": "5"}
            ]
        """.trimIndent()

        val body = json.toResponseBody("application/json".toMediaType())
        val parsed = DualModePageParser.parse(body, moshi, Repuesto::class.java)

        val items = (parsed as DualModePageParser.ParsedPage.LegacyList).items
        assertEquals(1, items.size)
        assertEquals("1", items[0].idRepuesto)
        assertEquals(5, items[0].cantidadDisponible)
    }

    @Test
    fun `parsea wrapper paginado cuando la respuesta es un objeto con data`() {
        val json = """
            {
              "data": [
                {"id_repuesto": true, "nombre_repuesto": "Bujia", "cantidad_disponible": 3}
              ],
              "meta": {"page": 1, "per_page": 15, "total": 1, "total_pages": 1, "has_prev": false, "has_next": false}
            }
        """.trimIndent()

        val body = json.toResponseBody("application/json".toMediaType())
        val parsed = DualModePageParser.parse(body, moshi, Repuesto::class.java)

        val page = (parsed as DualModePageParser.ParsedPage.Paged).page
        assertEquals(1, page.data.size)
        assertEquals("true", page.data[0].idRepuesto)
        assertEquals(3, page.data[0].cantidadDisponible)
    }
}
