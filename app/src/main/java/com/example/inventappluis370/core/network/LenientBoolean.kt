package com.example.inventappluis370.core.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

/**
 * Marca campos Boolean que pueden venir como String ("1", "true") o Number (1, 0)
 * en el backend legacy.
 */
@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class LenientBoolean

object LenientBooleanAdapter : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != Boolean::class.javaObjectType && type != Boolean::class.java) return null
        if (annotations.none { it is LenientBoolean }) return null

        return object : JsonAdapter<Boolean>() {
            override fun fromJson(reader: JsonReader): Boolean? {
                return when (reader.peek()) {
                    JsonReader.Token.BOOLEAN -> reader.nextBoolean()
                    JsonReader.Token.STRING -> {
                        val value = reader.nextString().lowercase()
                        value == "true" || value == "1" || value == "yes" || value == "si"
                    }
                    JsonReader.Token.NUMBER -> {
                        reader.nextInt() != 0
                    }
                    JsonReader.Token.NULL -> reader.nextNull<Boolean>()
                    else -> {
                        reader.skipValue()
                        null
                    }
                }
            }

            override fun toJson(writer: JsonWriter, value: Boolean?) {
                writer.value(value)
            }
        }
    }
}
