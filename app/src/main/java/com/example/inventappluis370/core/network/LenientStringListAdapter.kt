package com.example.inventappluis370.core.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

/**
 * Adapter Moshi para tolerar `List<String>` inconsistentes provenientes de Mongo.
 *
 * Casos observados:
 * - `tipos_notificacion` a veces viene como String ("servicios") o CSV ("servicios,repuestos")
 *   aunque el canónico sea array.
 *
 * Importante: NO usar @FromJson con parámetro `Any?` porque eso registra un adapter genérico
 * para `Object` y puede causar recursión infinita (StackOverflowError) al parsear.
 */
object LenientStringListAdapter : JsonAdapter.Factory {

    private val targetType: Type = Types.newParameterizedType(List::class.java, String::class.java)

    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != targetType) return null
        if (annotations.isNotEmpty()) return null // no manejamos qualifiers

        return object : JsonAdapter<List<String>>() {
            override fun fromJson(reader: JsonReader): List<String>? {
                return when (reader.peek()) {
                    JsonReader.Token.NULL -> reader.nextNull<List<String>>()
                    JsonReader.Token.BEGIN_ARRAY -> {
                        val out = mutableListOf<String>()
                        reader.beginArray()
                        while (reader.hasNext()) {
                            when (reader.peek()) {
                                JsonReader.Token.NULL -> reader.nextNull<Unit>()
                                JsonReader.Token.STRING -> out += reader.nextString().trim()
                                JsonReader.Token.NUMBER -> out += reader.nextDouble().toString()
                                JsonReader.Token.BOOLEAN -> out += reader.nextBoolean().toString()
                                else -> reader.skipValue()
                            }
                        }
                        reader.endArray()
                        out.filter { it.isNotEmpty() }
                    }
                    JsonReader.Token.STRING -> {
                        val trimmed = reader.nextString().trim()
                        if (trimmed.isEmpty()) emptyList()
                        else trimmed.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                    }
                    else -> {
                        reader.skipValue()
                        emptyList()
                    }
                }
            }

            override fun toJson(writer: JsonWriter, value: List<String>?) {
                if (value == null) {
                    writer.nullValue()
                    return
                }
                writer.beginArray()
                value.forEach { writer.value(it) }
                writer.endArray()
            }
        }
    }
}
