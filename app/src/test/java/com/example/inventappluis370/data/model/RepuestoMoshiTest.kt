package com.example.inventappluis370.data.model

import com.example.inventappluis370.core.network.LenientAnyToStringAdapter
import com.example.inventappluis370.core.network.LenientDoubleAdapter
import com.example.inventappluis370.core.network.LenientIntAdapter
import com.example.inventappluis370.core.network.LenientStringListAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class RepuestoMoshiTest {

    private val moshi: Moshi = Moshi.Builder()
        .add(LenientIntAdapter)
        .add(LenientDoubleAdapter)
        .add(LenientStringListAdapter)
        .add(LenientAnyToStringAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `parsea Repuesto cuando id_repuesto viene como numero y cantidad como string`() {
        val json = """
            {
              "id_repuesto": 123,
              "nombre_repuesto": "Filtro",
              "cantidad_disponible": "5",
              "costo_unitario": "10.5",
              "nivel_critico": 1
            }
        """.trimIndent()

        val adapter = moshi.adapter(Repuesto::class.java)
        val repuesto = adapter.fromJson(json)!!

        assertEquals("123", repuesto.idRepuesto)
        assertEquals("Filtro", repuesto.nombreRepuesto)
        assertEquals(5, repuesto.cantidadDisponible)
        assertEquals(10.5, repuesto.costoUnitario ?: 0.0, 0.0001)
        assertEquals(1, repuesto.nivelCritico)
    }

    @Test
    fun `parsea Repuesto cuando id_repuesto viene como boolean`() {
        val json = """
            {
              "id_repuesto": true,
              "nombre_repuesto": "Bujia",
              "cantidad_disponible": 3
            }
        """.trimIndent()

        val adapter = moshi.adapter(Repuesto::class.java)
        val repuesto = adapter.fromJson(json)!!

        assertEquals("true", repuesto.idRepuesto)
        assertEquals(3, repuesto.cantidadDisponible)
    }
}
