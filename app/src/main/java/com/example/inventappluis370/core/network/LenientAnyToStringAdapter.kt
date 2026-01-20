package com.example.inventappluis370.core.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

/**
 * Adapter leniente para *campos específicos* `String` anotados con [LenientString]
 * que en backend legacy pueden venir como Number/Boolean/Null.
 *
 * Importante: antes esto estaba implementado con métodos @FromJson que aceptaban `Any?`.
 * Eso hace que Moshi lo trate como adapter genérico de `Object` y puede provocar recursión
 * infinita (StackOverflowError) al parsear respuestas.
 */
object LenientAnyToStringAdapter : JsonAdapter.Factory {

    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != String::class.java) return null
        if (annotations.none { it is LenientString }) return null

        // Consumimos nuestra anotación para que Moshi no siga buscando otro adapter con ella.
        val delegateAnnotations = annotations.filterNot { it is LenientString }.toSet()
        val delegate = moshi.nextAdapter<String>(this, type, delegateAnnotations)

        return object : JsonAdapter<String>() {
            override fun fromJson(reader: JsonReader): String? {
                return when (reader.peek()) {
                    JsonReader.Token.NULL -> reader.nextNull<String>()
                    JsonReader.Token.STRING -> reader.nextString()
                    JsonReader.Token.NUMBER -> reader.nextDouble().toString()
                    JsonReader.Token.BOOLEAN -> reader.nextBoolean().toString()
                    // Si llega un objeto/array, evitamos explotar: lo saltamos y devolvemos string vacía.
                    JsonReader.Token.BEGIN_ARRAY, JsonReader.Token.BEGIN_OBJECT -> {
                        reader.skipValue()
                        ""
                    }
                    else -> delegate.fromJson(reader)
                }
            }

            override fun toJson(writer: JsonWriter, value: String?) {
                if (value == null) {
                    writer.nullValue()
                } else {
                    writer.value(value)
                }
            }
        }
    }
}
