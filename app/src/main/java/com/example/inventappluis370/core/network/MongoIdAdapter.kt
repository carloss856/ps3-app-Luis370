package com.example.inventappluis370.core.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

/**
 * Adapter leniente para campos String anotados con [MongoId].
 *
 * Soporta:
 * - "507f1f77bcf86cd799439011" (string)
 * - {"$oid":"507f1f77bcf86cd799439011"} (Mongo Extended JSON)
 *
 * Importante: es *específico* por anotación para no interferir con otros Strings.
 */
object MongoIdAdapter : JsonAdapter.Factory {

    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != String::class.java) return null
        if (annotations.none { it is MongoId }) return null

        // Consumimos nuestra anotación para que Moshi no siga buscando otro adapter con ella.
        val delegateAnnotations = annotations.filterNot { it is MongoId }.toSet()
        val delegate = moshi.nextAdapter<String>(this, type, delegateAnnotations)

        return object : JsonAdapter<String>() {
            override fun fromJson(reader: JsonReader): String? {
                return when (reader.peek()) {
                    JsonReader.Token.NULL -> reader.nextNull<String>()
                    JsonReader.Token.STRING -> normalize(reader.nextString())
                    JsonReader.Token.BEGIN_OBJECT -> {
                        var oid: String? = null
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "\$oid" -> {
                                    oid = when (reader.peek()) {
                                        JsonReader.Token.STRING -> reader.nextString()
                                        JsonReader.Token.NULL -> reader.nextNull<String>()
                                        else -> {
                                            reader.skipValue(); null
                                        }
                                    }
                                }
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()
                        normalize(oid)
                    }
                    else -> {
                        // Si llega number/array, evitamos crash: saltamos y delegamos si aplica.
                        // (Delegate típicamente fallará, pero preferimos try/catch.)
                        runCatching {
                            delegate.fromJson(reader)
                        }.getOrElse {
                            reader.skipValue(); null
                        }
                    }
                }
            }

            override fun toJson(writer: JsonWriter, value: String?) {
                if (value == null) writer.nullValue() else writer.value(value)
            }

            private fun normalize(raw: String?): String? {
                val t = raw?.trim()
                if (t.isNullOrEmpty() || t.equals("null", ignoreCase = true)) return null
                // Validación suave: ObjectId típico = 24 hex. Si no calza, igual devolvemos el valor.
                // Si quieres endurecerlo, cambia a throw JsonDataException.
                return t
            }
        }
    }
}
