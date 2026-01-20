package com.example.inventappluis370.core.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

/**
 * Adapters Moshi para tolerar datos inconsistentes de MongoDB.
 *
 * En la BD real se observaron números guardados como strings u objetos (Mongo Extended JSON):
 * - cantidad_disponible
 * - nivel_critico
 * - costo_unitario
 * - cantidad_entrada
 *
 * Importante: NO usar @FromJson con parámetro `Any?` porque eso registra un adapter genérico
 * para `Object` y puede provocar StackOverflowError.
 */
object LenientIntAdapter : JsonAdapter.Factory {

    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (annotations.isNotEmpty()) return null
        if (type != Int::class.javaObjectType) return null

        return object : JsonAdapter<Int>() {
            override fun fromJson(reader: JsonReader): Int? {
                return when (reader.peek()) {
                    JsonReader.Token.NULL -> reader.nextNull<Int>()
                    JsonReader.Token.NUMBER -> reader.nextDouble().toInt()
                    JsonReader.Token.STRING -> parseIntFromString(reader.nextString())
                    JsonReader.Token.BEGIN_OBJECT -> {
                        // Mongo Extended JSON: {"$numberInt":"10"} etc.
                        var inner: Any? = null
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "\$numberInt", "\$numberLong", "\$numberDouble" -> {
                                    inner = when (reader.peek()) {
                                        JsonReader.Token.STRING -> reader.nextString()
                                        JsonReader.Token.NUMBER -> reader.nextDouble()
                                        JsonReader.Token.NULL -> reader.nextNull<Any>()
                                        else -> {
                                            reader.skipValue(); null
                                        }
                                    }
                                }
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()

                        when (inner) {
                            null -> null
                            is Number -> inner.toInt()
                            is String -> parseIntFromString(inner)
                            else -> throw JsonDataException("Expected Int in extended-json but was ${inner::class.java}")
                        }
                    }
                    else -> {
                        reader.skipValue()
                        null
                    }
                }
            }

            override fun toJson(writer: JsonWriter, value: Int?) {
                if (value == null) writer.nullValue() else writer.value(value)
            }
        }
    }

    private fun parseIntFromString(raw: String): Int? {
        val normalized = raw.trim()
        if (normalized.isEmpty() || normalized.equals("null", ignoreCase = true)) return null
        val cleaned = normalized.replace(',', '.')
        return cleaned.toIntOrNull()
            ?: cleaned.toDoubleOrNull()?.toInt()
            ?: throw JsonDataException("Expected Int as String but was '$raw'")
    }
}

object LenientDoubleAdapter : JsonAdapter.Factory {

    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (annotations.isNotEmpty()) return null
        if (type != Double::class.javaObjectType) return null

        return object : JsonAdapter<Double>() {
            override fun fromJson(reader: JsonReader): Double? {
                return when (reader.peek()) {
                    JsonReader.Token.NULL -> reader.nextNull<Double>()
                    JsonReader.Token.NUMBER -> reader.nextDouble()
                    JsonReader.Token.STRING -> parseDoubleFromString(reader.nextString())
                    JsonReader.Token.BEGIN_OBJECT -> {
                        var inner: Any? = null
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "\$numberDouble", "\$numberInt", "\$numberLong" -> {
                                    inner = when (reader.peek()) {
                                        JsonReader.Token.STRING -> reader.nextString()
                                        JsonReader.Token.NUMBER -> reader.nextDouble()
                                        JsonReader.Token.NULL -> reader.nextNull<Any>()
                                        else -> {
                                            reader.skipValue(); null
                                        }
                                    }
                                }
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()

                        when (inner) {
                            null -> null
                            is Number -> inner.toDouble()
                            is String -> parseDoubleFromString(inner)
                            else -> throw JsonDataException("Expected Double in extended-json but was ${inner::class.java}")
                        }
                    }
                    else -> {
                        reader.skipValue()
                        null
                    }
                }
            }

            override fun toJson(writer: JsonWriter, value: Double?) {
                if (value == null) writer.nullValue() else writer.value(value)
            }
        }
    }

    private fun parseDoubleFromString(raw: String): Double? {
        val normalized = raw.trim()
        if (normalized.isEmpty() || normalized.equals("null", ignoreCase = true)) return null
        val cleaned = normalized.replace(',', '.')
        return cleaned.toDoubleOrNull() ?: throw JsonDataException("Expected Double as String but was '$raw'")
    }
}
